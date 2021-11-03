package com.huawei.apm.core.lubanops.bootstrap.plugin.common.url;

import java.util.List;
import java.util.Map;

import com.huawei.apm.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.SinglePrimaryKeyAggregator;

public class UrlStatusGroupAggregator extends SinglePrimaryKeyAggregator<StatusCodeStats> {

    @Override
    public String getName() {
        return "statuscode";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    @Override
    protected Class<StatusCodeStats> getValueType() {
        return StatusCodeStats.class;
    }

    public StatusCodeStats onCode(int code) {
        return getValue(code + "");
    }

    @Override
    protected String primaryKey() {
        return "code";
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

}
