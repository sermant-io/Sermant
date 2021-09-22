package com.lubanops.apm.bootstrap.plugin.common.url;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.lubanops.apm.bootstrap.collector.api.MonitorDataRow;
import com.lubanops.apm.bootstrap.collector.api.StatsBase;
import com.lubanops.apm.bootstrap.utils.HarvestUtil;

public class StatusCodeStats implements StatsBase {

    private volatile long countOld;

    private AtomicLong count = new AtomicLong(0);

    private AtomicReference<String> url = new AtomicReference<String>();

    public AtomicLong getCount() {
        return count;
    }

    public void setCount(AtomicLong count) {
        this.count = count;
    }

    public AtomicReference<String> getUrl() {
        return url;
    }

    public void setUrl(AtomicReference<String> url) {
        this.url = url;
    }

    public MonitorDataRow harvest() {
        MonitorDataRow row = new MonitorDataRow();
        countOld = HarvestUtil.getMetricCount(count, countOld, "count", row);
        row.put("url", url.getAndSet(null));
        return row;
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = new MonitorDataRow();
        row.put("count", count.get());
        row.put("url", url.get());
        return row;
    }

}
