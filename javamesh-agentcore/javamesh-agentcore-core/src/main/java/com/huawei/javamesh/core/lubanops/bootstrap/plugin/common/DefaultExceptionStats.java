package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.StatsBase;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.ExceptionUtil;

public class DefaultExceptionStats implements StatsBase {
    // 记录上一次的次数
    private volatile int lastCount;

    // 存储总数，不清空
    private AtomicInteger count = new AtomicInteger(0);

    private AtomicReference<String> message = new AtomicReference<String>();

    private AtomicReference<String> stackTrace = new AtomicReference<String>();

    private AtomicReference<String> content = new AtomicReference<String>();

    public void onThrowable(Throwable t, String content) {
        count.incrementAndGet();
        if (message.get() == null) {
            message.set(t.getMessage());
        }
        if (stackTrace.get() == null) {
            String s = ExceptionUtil.getThrowableStackTrace(t, false);
            stackTrace.set(s);
        }
        if (this.content.get() == null) {
            this.content.set(content);
        }
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = new MonitorDataRow();
        row.put("count", count.get());
        String m = message.get();
        if (m != null) {
            row.put("message", m);
        }
        String st = stackTrace.get();
        if (st != null) {
            row.put("stackTrace", st);
        }
        String ct = content.get();
        if (ct != null) {
            row.put("content", ct);
        }
        return row;
    }

    @Override
    public MonitorDataRow harvest() {
        int c = count.get();
        int delta;
        if ((delta = c - lastCount) > 0) {
            MonitorDataRow row = new MonitorDataRow();
            row.put("count", delta);
            lastCount = c;
            String m = message.getAndSet(null);
            if (m != null) {
                row.put("message", m);
            }
            String st = stackTrace.getAndSet(null);
            if (st != null) {
                row.put("stackTrace", st);
            }
            String ct = content.getAndSet(null);
            if (ct != null) {
                row.put("content", ct);
            }
            return row;
        }
        return null;
    }
}
