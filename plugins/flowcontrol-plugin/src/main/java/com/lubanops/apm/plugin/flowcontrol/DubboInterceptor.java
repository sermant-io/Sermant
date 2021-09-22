package com.lubanops.apm.plugin.flowcontrol;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.exception.FlowControlException;
import com.lubanops.apm.plugin.flowcontrol.util.SentinelRuleUtil;
import com.lubanops.apm.plugin.flowcontrol.util.TraceEntryUtils;

import java.lang.reflect.Method;
import java.util.Locale;

public abstract class DubboInterceptor implements InstanceMethodInterceptor {
    protected final ThreadLocal<Entry> entryThreadProviderLocal = new ThreadLocal<Entry>();
    protected final ThreadLocal<Entry> entryThreadConsumerLocal = new ThreadLocal<Entry>();

    @Override
    public void onThrow(Object obj, Method method, Object[] allArguments, Throwable t) {
        TraceEntryUtils.traceEntry(entryThreadConsumerLocal, entryThreadProviderLocal, t);
        RecordLog.error("[DubboInterceptor] exception：" + t.toString());
    }

    protected void handleException(Throwable throwable) {
        TraceEntryUtils.traceEntry(entryThreadConsumerLocal,
            entryThreadProviderLocal, throwable);
    }

    protected void removeThreadLocalEntry() {
        // 消费者 sentinel entry 退出 释放资源
        if (entryThreadConsumerLocal.get() != null) {
            entryThreadConsumerLocal.get().exit();
            entryThreadConsumerLocal.remove();
        }

        // 生产者 sentinel entry 退出 释放资源
        if (entryThreadProviderLocal.get() != null) {
            entryThreadProviderLocal.get().exit();
            entryThreadProviderLocal.remove();
        }
    }

    protected String getResourceName(String interfaceName, String methodName) {
        StringBuilder buf = new StringBuilder();
        buf.append(interfaceName)
            .append(":")
            .append(methodName);
        return buf.toString();
    }

    protected void handleBlockException(BlockException ex, String resourceName, BeforeResult result, String type) {
        try {
            final String msg = String.format(Locale.ENGLISH,
                "[%s] has been blocked! [appName=%s, resourceName=%s]",
                type, ex.getRuleLimitApp(), resourceName);
            RecordLog.info(msg);
            String res = SentinelRuleUtil.getResult(ex.getRule());
            result.setResult(res);
            throw new FlowControlException(res);
        } finally {
            removeThreadLocalEntry();
        }
    }

    protected void entry(boolean isConsumerSide, String interfaceName, String methodName, BeforeResult result) {
        String resourceName = getResourceName(interfaceName, methodName);
        Entry entry;
        if (isConsumerSide) {
            // 消费者
            try {
                entry = SphU.entry(resourceName, EntryType.OUT);
                entryThreadConsumerLocal.set(entry);
            } catch (BlockException ex) {
                handleBlockException(ex, resourceName, result, "ApacheDubboInterceptor consumer");
            }
        } else {
            // 生产者
            try {
                entry = SphU.entry(resourceName, EntryType.IN);
                entryThreadProviderLocal.set(entry);
            } catch (BlockException ex) {
                handleBlockException(ex, resourceName, result, "ApacheDubboInterceptor provider");
            }
        }
    }
}
