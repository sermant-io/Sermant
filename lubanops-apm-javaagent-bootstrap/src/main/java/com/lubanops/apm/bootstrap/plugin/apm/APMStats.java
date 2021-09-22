package com.lubanops.apm.bootstrap.plugin.apm;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.lubanops.apm.bootstrap.collector.api.MonitorDataRow;
import com.lubanops.apm.bootstrap.collector.api.StatsBase;
import com.lubanops.apm.bootstrap.utils.ConcurrentUtil;

/**
 * apm自身监控详细数据 <br>
 */
public class APMStats implements StatsBase {

    protected AtomicLong sendCount = new AtomicLong(0);

    protected AtomicLong discardCount = new AtomicLong(0);

    protected AtomicLong errorCount = new AtomicLong(0);

    protected AtomicLong maxBytes = new AtomicLong(0);

    protected AtomicLong sendBytes = new AtomicLong(0);

    protected AtomicLong discardBytes = new AtomicLong(0);

    protected AtomicLong errorBytes = new AtomicLong(0);

    protected AtomicInteger maxQueueSize = new AtomicInteger(0);

    protected AtomicLong sendTotalTime = new AtomicLong(0);

    protected AtomicLong slowTime = new AtomicLong(0);

    public void onStart(int queueSize) {
        sendCount.incrementAndGet();
        ConcurrentUtil.setMaxValue(maxQueueSize, queueSize);
    }

    public void onDiscard(long bytes) {
        sendBytes.addAndGet(bytes);
        ConcurrentUtil.setMaxValue(maxBytes, bytes);
        discardCount.incrementAndGet();
        discardBytes.addAndGet(bytes);
    }

    public void onThrowable(long bytes) {
        sendBytes.addAndGet(bytes);
        ConcurrentUtil.setMaxValue(maxBytes, bytes);
        errorCount.incrementAndGet();
        errorBytes.addAndGet(bytes);
    }

    public void onFinally(long useTime) {
        sendTotalTime.addAndGet(useTime);
        ConcurrentUtil.setMaxValue(slowTime, useTime);
    }

    public void onSuccess(long bytes) {
        sendBytes.addAndGet(bytes);
        ConcurrentUtil.setMaxValue(maxBytes, bytes);
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = new MonitorDataRow();
        row.put(DetailAggregator.AGGREAGET_METRIC_SENDCOUNT, sendCount.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_DISCARDCOUNT, discardCount.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_ERRORCOUNT, errorCount.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_MAXBYTES, maxBytes.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_SENDBYTES, sendBytes.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_DISCARDBYTES, discardBytes.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_ERRORBYTES, errorBytes.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_MAXQUEUESIZE, maxQueueSize.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_SENDTOTALTIME, sendTotalTime.get());
        row.put(DetailAggregator.AGGREAGET_METRIC_SLOWTIME, slowTime.get());
        return row;
    }

    public double getSuccessPercent() {

        long cSendCount = sendCount.get();

        long cErrorCount = errorCount.get();
        if (cSendCount != 0) {
            return (double) cErrorCount / (double) cSendCount;
        } else {
            return 0;
        }

    }

    public double getSendCount() {
        long cSendCount = sendCount.get();
        return cSendCount;
    }

    public long getErrorCount() {
        long cErrorCount = errorCount.get();
        return cErrorCount;
    }

    @Override
    public MonitorDataRow harvest() {
        long cSendCount = sendCount.getAndSet(0);
        long cDiscardCountHarvest = discardCount.getAndSet(0);
        long cErrorCount = errorCount.getAndSet(0);
        long cMaxBytes = maxBytes.getAndSet(0);
        long cSendBytes = sendBytes.getAndSet(0);
        long cDiscardBytes = discardBytes.getAndSet(0);
        long cErrorBytes = errorBytes.getAndSet(0);
        long cMaxQueueSize = maxQueueSize.getAndSet(0);
        long cSendTotalTime = sendTotalTime.getAndSet(0);
        long cSlowTime = slowTime.getAndSet(0);

        MonitorDataRow row = new MonitorDataRow();
        row.put(DetailAggregator.AGGREAGET_METRIC_SENDCOUNT, cSendCount);
        row.put(DetailAggregator.AGGREAGET_METRIC_DISCARDCOUNT, cDiscardCountHarvest);
        row.put(DetailAggregator.AGGREAGET_METRIC_ERRORCOUNT, cErrorCount);
        row.put(DetailAggregator.AGGREAGET_METRIC_MAXBYTES, cMaxBytes);
        row.put(DetailAggregator.AGGREAGET_METRIC_SENDBYTES, cSendBytes);
        row.put(DetailAggregator.AGGREAGET_METRIC_DISCARDBYTES, cDiscardBytes);
        row.put(DetailAggregator.AGGREAGET_METRIC_ERRORBYTES, cErrorBytes);
        row.put(DetailAggregator.AGGREAGET_METRIC_MAXQUEUESIZE, cMaxQueueSize);
        row.put(DetailAggregator.AGGREAGET_METRIC_SENDTOTALTIME, cSendTotalTime);
        row.put(DetailAggregator.AGGREAGET_METRIC_SLOWTIME, cSlowTime);
        return row;
    }
}
