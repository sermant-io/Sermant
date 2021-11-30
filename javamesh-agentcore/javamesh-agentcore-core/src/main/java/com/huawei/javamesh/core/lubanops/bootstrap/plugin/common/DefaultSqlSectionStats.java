package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.ExceptionUtil;

public class DefaultSqlSectionStats extends DefaultSectionStats {
    /**
     * 存储当前值
     */
    private final AtomicLong updatedRowCount = new AtomicLong(0L);

    private final AtomicLong readRowCount = new AtomicLong(0L); /**读取行数*/

    /**
     * 慢SQL
     */
    private final AtomicReference<String> lastSlowSql = new AtomicReference<String>();

    /**
     * 错SQL
     */
    private final AtomicReference<String> lastErrorSql = new AtomicReference<String>();

    /**
     * 错误堆栈
     */
    private final AtomicReference<String> lastError = new AtomicReference<String>();

    /**
     * 慢traceId
     */
    private final AtomicReference<String> slowTraceId = new AtomicReference<String>();

    /**
     * 错traceId
     */
    private final AtomicReference<String> errorTraceId = new AtomicReference<String>();

    /**
     * 记录上一次读取的值，用于获取差值
     */
    private volatile long updatedRowCountOld;

    private volatile long readRowCountOld;

    public void onThrowable(String sql, Throwable t) {
        super.onThrowable(t);
        if (lastError.get() == null) {
            String stackTrace = ExceptionUtil.getThrowableStackTrace(t, false);
            lastError.set(stackTrace);
        }
        if (lastErrorSql.get() == null) {
            lastErrorSql.set(sql);
        }
    }

    public boolean onFinally(String sql, long timeInNanos, int updatedRowCount, int readRowCount) {
        this.updatedRowCount.addAndGet(updatedRowCount);
        this.readRowCount.addAndGet(readRowCount);
        boolean flag = super.onFinally(timeInNanos);
        if (flag && lastSlowSql.get() == null) {
            lastSlowSql.set(sql);
        }
        return flag;
    }

    public void setSlowTraceId(String slowTraceId) {
        if (slowTraceId != null) {
            this.slowTraceId.set(slowTraceId);
        }
    }

    public void setErrorTraceId(String errorTraceId) {
        if (errorTraceId != null) {
            this.errorTraceId.set(errorTraceId);
        }
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = super.getStatus();
        row.put("updatedRowCount", updatedRowCount.get());
        row.put("readRowCount", readRowCount.get());
        row.put("lastSlowSql", lastSlowSql.get());
        row.put("lastErrorSql", lastErrorSql.get());
        row.put("lastError", lastError.get());
        row.put("slowTraceId", slowTraceId.get());
        row.put("errorTraceId", errorTraceId.get());
        return row;
    }

    @Override
    public MonitorDataRow harvest(int[] newRanges) {
        MonitorDataRow row = super.harvest(newRanges);
        if (row != null) {
            long updatedRowCountNew = updatedRowCount.get();
            row.put("updatedRowCount", updatedRowCountNew - updatedRowCountOld);
            long readRowCountNew = readRowCount.get();
            row.put("readRowCount", readRowCountNew - readRowCountOld);
            row.put("lastSlowSql", lastSlowSql.getAndSet(null));
            row.put("lastErrorSql", lastErrorSql.getAndSet(null));
            row.put("lastError", lastError.getAndSet(null));
            row.put("slowTraceId", slowTraceId.getAndSet(null));
            row.put("errorTraceId", errorTraceId.getAndSet(null));

            // reset
            updatedRowCountOld = updatedRowCountNew;
            readRowCountOld = readRowCountNew;
        }
        return row;
    }
}
