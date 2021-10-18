package com.huawei.apm.bootstrap.lubanops.plugin.common.url;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.huawei.apm.bootstrap.lubanops.collector.api.MonitorDataRow;
import com.huawei.apm.bootstrap.lubanops.plugin.common.DefaultSectionStats;

public class UrlStats extends DefaultSectionStats {

    public volatile boolean has200 = false;

    private AtomicReference<String> maxTimeUsedUrl = new AtomicReference<String>();// httpRequest中原生的url没有经过agent映射的

    private AtomicInteger sampleCount = new AtomicInteger(0);

    public long onFinally(long start, String nativeUrl, boolean isError) {
        long endTime = System.nanoTime();
        long useTime = endTime - start;
        boolean isMaxTime = super.onFinally(useTime);
        if (isMaxTime) {
            maxTimeUsedUrl.set(nativeUrl);
        }
        if (isError) {
            errorCountIncrement();
        }
        return useTime;
    }

    private void errorCountIncrement() {
        this.errorCount.incrementAndGet();
    }

    public String harvestMaxTimeNativeUrl() {
        return maxTimeUsedUrl.getAndSet(null);
    }

    public void runningCountDecrement() {
        runningCount.decrementAndGet();
    }

    @Override
    public MonitorDataRow harvest(int[] newRanges) {
        sampleCount.set(0);
        return super.harvest(newRanges);
    }

    public AtomicInteger getSampleCount() {
        return sampleCount;
    }

    public long getInvokeCount() {
        return invokeCount.get() - invokeCountOld;
    }

}
