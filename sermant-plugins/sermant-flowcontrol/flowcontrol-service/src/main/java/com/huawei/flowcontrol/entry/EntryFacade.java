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

import com.huawei.flowcontrol.common.adapte.cse.match.MatchManager;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 基于sentinel进行数据统计 并对当前线程的resource entry进行处理
 *
 * @author zhouss
 * @since 2021-11-25
 */
public enum EntryFacade {
    /**
     * 单例
     */
    INSTANCE;

    private final boolean isUseCseRule;

    private final HttpEntry httpEntry = new HttpEntry();

    private final DubboEntry dubboEntry = new DubboEntry();

    EntryFacade() {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        isUseCseRule = pluginConfig.isUseCseRule();
    }

    /**
     * ======================================HTTP================================= http请求记录
     *
     * @param request 请求数据
     * @throws BlockException 触发流控抛出
     */
    public void tryEntry(RequestEntity request) throws BlockException {
        httpEntry.tryEntry(request);
    }

    /**
     * apache 数据记录
     *
     * @param request 请求信息
     * @param isProvider 是否为生产者
     * @throws BlockException 触发流控抛出
     */
    public void tryEntry(RequestEntity request, boolean isProvider) throws BlockException {
        dubboEntry.tryEntry(request, isProvider);
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
     * 异常记录
     *
     * @param throwable 异常信息
     * @param isProvider 是否为生产端
     */
    public void tryTraceEntry(Throwable throwable, boolean isProvider) {
        dubboEntry.tryTraceEntry(throwable, isProvider);
    }

    /**
     * http entry退出方法
     */
    public void exit() {
        httpEntry.exit();
    }

    /**
     * dubbo entry退出方法
     */
    public void exitDubbo() {
        dubboEntry.exit();
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

    static class DubboEntry implements SimpleEntry {
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

        private void exit(List<Entry> entries) {
            if (entries == null || entries.isEmpty()) {
                return;
            }
            for (int i = entries.size() - 1; i >= 0; i--) {
                entries.get(i).exit();
            }
        }

        protected void setEntry(List<Entry> entries, boolean isProvider) {
            if (isProvider) {
                providerEntries.set(entries);
            } else {
                consumerEntries.set(entries);
            }
        }

        /**
         * apache dubbo
         *
         * @param request 调用信息
         */
        public void tryEntry(RequestEntity request, boolean isProvider) throws BlockException {
            if (INSTANCE.isUseCseRule) {
                // cse适配
                final Set<String> matchBusinesses = MatchManager.INSTANCE.match(request);
                entryWithCse(matchBusinesses, isProvider);
            } else {
                final Entry entry = SphU.entry(request.getApiPath(), isProvider ? EntryType.IN : EntryType.OUT);
                setEntry(Collections.singletonList(entry), isProvider);
            }
        }
    }

    class HttpEntry implements SimpleEntry {
        private final ThreadLocal<List<Entry>> threadLocalEntries = new ThreadLocal<List<Entry>>();

        /**
         * 统计方法 针对HTTP请求
         *
         * @param request 请求
         * @throws BlockException 流控异常
         */
        public void tryEntry(RequestEntity request) throws BlockException {
            if (EntryFacade.this.isUseCseRule) {
                // 开启cse兼容
                // 1.匹配该请求的业务场景
                final Set<String> matchBusinesses = MatchManager.INSTANCE.match(request);
                entryWithCse(matchBusinesses);
            } else {
                // 原逻辑拦截
                final Entry entry = SphU.entry(request.getApiPath(), EntryType.IN);
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
