/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.flowcontrol.config.CommonConst;
import com.huawei.flowcontrol.util.FilterUtil;
import com.huawei.flowcontrol.util.SentinelRuleUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * DispatcherServlet 的 API接口增强
 * 埋点定义sentinel资源
 *
 * @author liyi
 * @since 2020-08-26
 */
public class DispatcherServletInterceptor implements InstanceMethodInterceptor {
    static final ThreadLocal<Entry> ENTRY_THREAD_LOCAL = new ThreadLocal<Entry>();

    @Override
    public void before(Object obj, Method method, Object[] allArguments, BeforeResult result) throws Exception {
        Entry entry;
        HttpServletRequest req;
        if (allArguments[0] instanceof HttpServletRequest) {
            req = (HttpServletRequest) allArguments[0];
        } else {
            return;
        }
        String resourceName = FilterUtil.filterTarget(req);
        try {
            // 流控代码
            entry = SphU.entry(resourceName, EntryType.IN);
            ENTRY_THREAD_LOCAL.set(entry);
        } catch (BlockException ex) {
            try {
                RecordLog.info("[DispatcherServletInterceptor] has been blocked! "
                    + "appName= " + ex.getRuleLimitApp() + " resourceName=" + resourceName);
                HttpServletResponse resp = null;
                if (allArguments[1] instanceof HttpServletResponse) {
                    resp = (HttpServletResponse) allArguments[1];
                }
                String errMsg = SentinelRuleUtil.getResult(ex.getRule());
                if (resp != null) {
                    resp.setStatus(CommonConst.HTTP_STATUS_429);
                    resp.getWriter().write(errMsg);
                }
                // 不再执行业务代码
                result.setResult(null);
            } finally {
                Entry currentEntry = ENTRY_THREAD_LOCAL.get();
                if (currentEntry != null) {
                    currentEntry.exit();
                    ENTRY_THREAD_LOCAL.remove();
                }
            }
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] allArguments, Object ret) {
        try {
            Entry entry = ENTRY_THREAD_LOCAL.get();
            if (entry != null) {
                entry.exit();
            }
            return ret;
        } finally {
            if (ENTRY_THREAD_LOCAL.get() != null) {
                ENTRY_THREAD_LOCAL.remove();
            }
        }
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] allArguments, Throwable t) {
        Entry entry = ENTRY_THREAD_LOCAL.get();
        if (t != null) {
            if (entry != null) {
                Tracer.traceEntry(t, entry);
                entry.exit();
            }
            RecordLog.error("[DispatcherServletInterceptor] exception：" + t.getMessage());
        }
    }
}
