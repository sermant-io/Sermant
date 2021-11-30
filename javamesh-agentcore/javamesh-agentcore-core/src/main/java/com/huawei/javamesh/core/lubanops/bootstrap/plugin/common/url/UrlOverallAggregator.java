package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.url;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.NonePrimaryKeyAggregator;
import com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.DefaultStats;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.HarvestUtil;

public class UrlOverallAggregator extends NonePrimaryKeyAggregator {

    private volatile long totalRequestCountOld;

    private volatile long totalRequestTimeOld;

    private volatile long errorCountOld;

    private AtomicLong totalRequestCount = new AtomicLong(0);

    private AtomicLong totalRequestTime = new AtomicLong(0);

    private AtomicLong errorCount = new AtomicLong();

    @Override
    public String getName() {
        return "total";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    public void onTotal(long t) {
        totalRequestCount.incrementAndGet();
        totalRequestTime.addAndGet(t);
    }

    public void onError() {
        errorCount.incrementAndGet();
    }

    @Override
    public MonitorDataRow constructItemRow() {
        MonitorDataRow row = new MonitorDataRow();
        totalRequestCountOld = HarvestUtil.getMetricCount(totalRequestCount, totalRequestCountOld, "invokeCount",
            row);
        long newTotalTime = totalRequestTime.get();
        long totalTimeLong = newTotalTime - totalRequestTimeOld;
        totalRequestTimeOld = newTotalTime;
        if (totalTimeLong > 0) {
            row.put("totalTime", totalTimeLong / DefaultStats.NANO_TO_MILLI);
        } else {
            row.put("totalTime", null);
        }
        errorCountOld = HarvestUtil.getMetricCount(errorCount, errorCountOld, "errorCount", row);
        return row;
    }

    @Override
    public void clear() {

    }

    @Override
    public List<MonitorDataRow> getAllStatus() {
        List<MonitorDataRow> list = new ArrayList<MonitorDataRow>();
        MonitorDataRow row = new MonitorDataRow();
        row.put("invokeCount", totalRequestCount.get());
        row.put("totalTime", totalRequestTime.get() / DefaultStats.NANO_TO_MILLI);
        row.put("errorCount", errorCount.get());
        list.add(row);
        return list;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

}
