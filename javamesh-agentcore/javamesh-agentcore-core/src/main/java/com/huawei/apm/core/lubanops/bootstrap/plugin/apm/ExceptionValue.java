package com.huawei.apm.core.lubanops.bootstrap.plugin.apm;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.huawei.apm.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.StatsBase;
import com.huawei.apm.core.lubanops.bootstrap.utils.ExceptionUtil;

/**
 * apm自身监控异常数据 <br>
 *
 * @author
 */

public class ExceptionValue implements StatsBase {

    protected AtomicInteger count = new AtomicInteger(0);

    protected AtomicReference<String> message = new AtomicReference<String>();

    protected AtomicReference<String> stackTrace = new AtomicReference<String>();

    private long countLast = 0;

    public void onThrowable(Throwable t) {
        count.incrementAndGet();
        if (message.get() == null) {
            message.set(t.getMessage());
        }
        if (stackTrace.get() == null) {
            String s = ExceptionUtil.getThrowableStackTrace(t, false);
            stackTrace.set(s);
        }
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = new MonitorDataRow();
        row.put(ExceptionAggregator.AGGREGATE_METRIC_COUNT, count.get());
        row.put(ExceptionAggregator.AGGREGATE_METRIC_MESSAGE, message.get());
        row.put(ExceptionAggregator.AGGREGATE_METRIC_STACKTRACE, stackTrace.get());

        return row;
    }

    @Override
    public MonitorDataRow harvest() {
        long c = count.get();
        long countHarvest = c - countLast;
        if (countHarvest <= 0) {
            return null;
        }
        countLast = c;
        MonitorDataRow row = new MonitorDataRow();
        row.put(ExceptionAggregator.AGGREGATE_METRIC_COUNT, countHarvest);
        row.put(ExceptionAggregator.AGGREGATE_METRIC_MESSAGE, message.getAndSet(null));
        row.put(ExceptionAggregator.AGGREGATE_METRIC_STACKTRACE, stackTrace.getAndSet(null));
        return row;
    }
}
