/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.entry;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.dubbo.rpc.RpcContext;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.flowcontrol.adapte.cse.match.MatchManager;
import com.huawei.flowcontrol.core.config.FlowControlConfig;
import com.huawei.flowcontrol.util.FilterUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 基于sentinel进行数据统计
 * 并对当前线程的resource entry进行处理
 *
 * @author zhouss
 * @since 2021-11-25
 */
public enum EntryFacade {
    /**
     * 单例
     */
    INSTANCE;

    private final boolean useCseRule;

    private final HttpEntry httpEntry = new HttpEntry();

    private final ApacheDubboEntry apacheDubboEntry = new ApacheDubboEntry();

    private final AlibabaDubboEntry alibabaDubboEntry = new AlibabaDubboEntry();

    EntryFacade() {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        useCseRule = pluginConfig.isUseCseRule();
    }

    //======================================HTTP=================================//

    /**
     * http请求记录
     *
     * @param request 请求数据
     * @throws BlockException 触发流控抛出
     */
    public void tryEntry(HttpServletRequest request) throws BlockException {
        httpEntry.tryEntry(request);
    }

    /**
     * http-记录异常
     *
     * @param throwable 异常信息
     */
    public void tryTraceEntry(Throwable throwable) {
        httpEntry.tryTraceEntry(throwable);
    }

    /**
     * http entry退出方法
     */
    public void exit() {
        httpEntry.exit();
    }

    //================================dubbo================================//

    /**
     * apache 数据记录
     *
     * @param invocation 请求信息
     * @throws BlockException 触发流控抛出
     */
    public void tryEntry(org.apache.dubbo.rpc.Invocation invocation) throws BlockException {
        apacheDubboEntry.tryEntry(invocation);
    }

    /**
     * alibaba 数据记录
     *
     * @param invocation 请求信息
     * @throws BlockException 触发流控抛出
     */
    public void tryEntry(com.alibaba.dubbo.rpc.Invocation invocation) throws BlockException {
        alibabaDubboEntry.tryEntry(invocation);
    }

    /**
     * 异常记录
     *
     * @param throwable 异常信息
     * @param isProvider 是否为生产端
     * @param dubboType dubbo类型
     */
    public void tryTraceEntry(Throwable throwable, boolean isProvider, DubboType dubboType) {
        if (dubboType == DubboType.ALIBABA) {
            alibabaDubboEntry.tryTraceEntry(throwable, isProvider);
        } else {
            apacheDubboEntry.tryTraceEntry(throwable, isProvider);
        }
    }

    /**
     * dubbo entry退出方法
     *
     * @param dubboType dubbo类型
     */
    public void exit(DubboType dubboType) {
        if (dubboType == DubboType.ALIBABA) {
            alibabaDubboEntry.exit();
        } else {
            apacheDubboEntry.exit();
        }
    }

    public enum DubboType {
        /**
         * alibaba dubbo
         */
        ALIBABA,

        /**
         * apache dubbo
         */
        APACHE
    }

    interface SimpleEntry {
    }

    abstract static class DubboEntry implements SimpleEntry {
        /**
         * provider端entry
         */
        protected final ThreadLocal<List<Entry>> providerEntries = new ThreadLocal<List<Entry>>();

        /**
         * consumer端entry
         */
        protected final ThreadLocal<List<Entry>> consumerEntries = new ThreadLocal<List<Entry>>();

        /**
         * cse场景数据记录
         *
         * @param matchBusinesses 已匹配场景列表
         * @param isProvider 是否为生产端
         * @throws BlockException 触发流控抛出
         */
        public void entryWithCse(Set<String> matchBusinesses, boolean isProvider) throws BlockException {
            if (matchBusinesses.isEmpty()) {
                return;
            }
            List<Entry> entries = isProvider ? providerEntries.get() : consumerEntries.get();
            if (entries == null) {
                entries = new ArrayList<Entry>();
            }
            for (String business : matchBusinesses) {
                final Entry entry = SphU.entry(business, isProvider ? EntryType.IN : EntryType.OUT);
                entries.add(entry);
            }
            setEntry(entries, isProvider);
        }

        /**
         * 记录异常
         *
         * @param throwable 异常
         * @param isProvider 是否为生产端
         */
        public void tryTraceEntry(Throwable throwable, boolean isProvider) {
            final List<Entry> entries = isProvider ? providerEntries.get() : consumerEntries.get();
            if (entries == null) {
                return;
            }
            for (Entry entry : entries) {
                Tracer.traceEntry(throwable, entry);
            }
        }

        /**
         * entry退出
         *
         */
        public void exit() {
            try {
                exit(consumerEntries.get());
                exit(providerEntries.get());
            } finally {
                consumerEntries.remove();
                providerEntries.remove();
            }
        }

        protected String getResourceName(String interfaceName, String version, String methodName) {
            // invocation.getTargetServiceUniqueName
            return MatchManager.INSTANCE.buildApiPath(interfaceName, version, methodName);
        }

        protected void setEntry(List<Entry> entries, boolean isProvider) {
            if (isProvider) {
                providerEntries.set(entries);
            } else {
                consumerEntries.set(entries);
            }
        }

        private void exit(List<Entry> entries) {
            if (entries == null || entries.isEmpty()) {
                return;
            }
            for (int i = entries.size() - 1; i >= 0; i--) {
                entries.get(i).exit();
            }
        }
    }

    class ApacheDubboEntry extends DubboEntry {

        /**
         * apache dubbo
         *
         * @param invocation 调用信息
         */
        public void tryEntry(org.apache.dubbo.rpc.Invocation invocation) throws BlockException {
            final org.apache.dubbo.rpc.RpcContext rpcContext = org.apache.dubbo.rpc.RpcContext.getContext();
            if (useCseRule) {
                // cse适配
                final Set<String> matchBusinesses = MatchManager.INSTANCE.matchApacheDubbo(invocation);
                entryWithCse(matchBusinesses, rpcContext.isProviderSide());
            } else {
                final String resourceName = getResourceName(invocation.getInvoker().getInterface().getName(),
                        invocation.getAttachment(MatchManager.DUBBO_ATTACHMENT_VERSION), invocation.getMethodName());
                final Entry entry = SphU.entry(resourceName, rpcContext.isProviderSide() ? EntryType.IN : EntryType.OUT);
                setEntry(Collections.singletonList(entry), rpcContext.isProviderSide());
            }
        }
    }

    class AlibabaDubboEntry extends DubboEntry {
        /**
         * alibaba dubbo
         *
         * @param invocation 调用信息
         */
        public void tryEntry(com.alibaba.dubbo.rpc.Invocation invocation) throws BlockException {
            final RpcContext rpcContext = RpcContext.getContext();
            if (useCseRule) {
                // cse适配
                final Set<String> matchBusinesses = MatchManager.INSTANCE.matchAlibabaDubbo(invocation);
                entryWithCse(matchBusinesses, rpcContext.isProviderSide());
            } else {
                final String resourceName = getResourceName(invocation.getInvoker().getInterface().getName(),
                        invocation.getAttachment(MatchManager.DUBBO_ATTACHMENT_VERSION), invocation.getMethodName());
                final Entry entry = SphU.entry(resourceName, rpcContext.isProviderSide() ? EntryType.IN : EntryType.OUT);
                setEntry(Collections.singletonList(entry), rpcContext.isProviderSide());
            }
        }
    }

    class HttpEntry implements SimpleEntry {
        private final ThreadLocal<List<Entry>> threadLocalEntries = new ThreadLocal<List<Entry>>();

        /**
         * 统计方法
         * 针对HTTP请求
         *
         * @param request 请求
         * @throws BlockException 流控异常
         */
        public void tryEntry(HttpServletRequest request) throws BlockException {
            if (EntryFacade.this.useCseRule) {
                // 开启cse兼容
                // 1.匹配该请求的业务场景
                final Set<String> matchBusinesses = MatchManager.INSTANCE.matchHttp(request);
                entryWithCse(matchBusinesses);
                // 2.执行
            } else {
                // 原逻辑拦截
                final Entry entry = SphU.entry(FilterUtil.filterTarget(request), EntryType.IN);
                threadLocalEntries.set(Collections.singletonList(entry));
            }
        }

        /**
         * 异常记录统计
         *
         * @param throwable 请求异常
         */
        public void tryTraceEntry(Throwable throwable) {
            final List<Entry> entries = threadLocalEntries.get();
            if (entries == null) {
                return;
            }
            for (Entry entry : entries) {
                Tracer.traceEntry(throwable, entry);
            }
        }

        /**
         * http退出方法
         */
        public void exit() {
            try {
                final List<Entry> entries = threadLocalEntries.get();
                if (entries == null) {
                    return;
                }
                for (int i = entries.size() - 1; i >= 0; i--) {
                    entries.get(i).exit();
                }
            } finally {
                threadLocalEntries.remove();
            }
        }

        private void entryWithCse(Set<String> matchBusinesses) throws BlockException {
            if (matchBusinesses.isEmpty()) {
                return;
            }
            List<Entry> entries = new ArrayList<Entry>();
            for (String business : matchBusinesses) {
                final Entry entry = SphU.entry(business, EntryType.IN);
                entries.add(entry);
            }
            threadLocalEntries.set(entries);
        }
    }
}
