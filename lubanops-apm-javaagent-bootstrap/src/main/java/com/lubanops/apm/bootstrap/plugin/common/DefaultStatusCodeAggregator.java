package com.lubanops.apm.bootstrap.plugin.common;

import java.util.List;
import java.util.Map;

import com.lubanops.apm.bootstrap.collector.api.MonitorDataRow;
import com.lubanops.apm.bootstrap.collector.api.SinglePrimaryKeyAggregator;

public class DefaultStatusCodeAggregator extends SinglePrimaryKeyAggregator<DefaultStatusCodeStats> {

    @Override
    public String getName() {
        return "code";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    protected String primaryKey() {
        return "code";
    }

    @Override
    protected Class<DefaultStatusCodeStats> getValueType() {
        return DefaultStatusCodeStats.class;
    }

    public void onStatusCode(String url, int code) {
        if (!isEnable) {
            return;
        }
        DefaultStatusCodeStats value = this.getValue(Integer.toString(code));
        value.getCount().incrementAndGet();
        value.getUrl().set(url);
    }
}
