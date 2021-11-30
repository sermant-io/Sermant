package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.StatsBase;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultStatusCodeStats implements StatsBase {
    // 记录上一次读取的值，用于获取差值
    private volatile long countOld;

    // 存储总数
    private AtomicLong count = new AtomicLong(0);

    private AtomicReference<String> url = new AtomicReference<String>();

    public AtomicLong getCount() {
        return count;
    }

    public AtomicReference<String> getUrl() {
        return url;
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = new MonitorDataRow();
        row.put("count", count.get());
        row.put("url", url.get());
        return row;
    }

    @Override
    public MonitorDataRow harvest() {
        long countNew = count.get();
        long countDelta;
        if ((countDelta = countNew - countOld) > 0) {
            MonitorDataRow row = new MonitorDataRow();
            row.put("count", countDelta);
            row.put("url", url.getAndSet(null));
            // reset
            countOld = countNew;
            return row;
        }
        return null;
    }
}
