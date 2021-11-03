package com.huawei.apm.core.lubanops.bootstrap.plugin.common;

import com.huawei.apm.core.lubanops.bootstrap.collector.api.SinglePrimaryKeyAggregator;
import com.huawei.apm.core.lubanops.bootstrap.collector.interceptor.AggregatorAroundInterceptor;

/**
 * 默认的单主键指标集聚合器
 */
public abstract class DefaultSinglePrimaryKeyAggregator extends SinglePrimaryKeyAggregator<DefaultSectionStats>
    implements AggregatorAroundInterceptor {
    private static ThreadLocal<DefaultSectionStats> statsLocal = new ThreadLocal<DefaultSectionStats>();

    private static ThreadLocal<Long> timeLocal = new ThreadLocal<Long>();

    @Override
    protected Class<DefaultSectionStats> getValueType() {
        return DefaultSectionStats.class;
    }

    @Override
    public long onStart(String primaryKey) {
        if (!isEnable) {
            return 0;
        }
        DefaultSectionStats stats = this.getValue(primaryKey);
        long start = stats.onStart();
        statsLocal.set(stats);
        timeLocal.set(start);
        return start;
    }

    @Override
    public void onThrowable(Throwable t) {
        if (!isEnable) {
            return;
        }
        DefaultSectionStats stat = statsLocal.get();
        if (stat != null) {
            stat.onThrowable(t);
        }
    }

    @Override
    public long onFinally() {
        if (!isEnable) {
            return 0;
        }
        DefaultSectionStats stat = statsLocal.get();
        Long start = timeLocal.get();
        if (stat != null && start != null) {
            long timeInNanos = System.nanoTime() - start;
            stat.onFinally(timeInNanos);
            statsLocal.set(null);
            timeLocal.set(null);
            return timeInNanos / 1000000;
        }
        return 0;
    }
}
