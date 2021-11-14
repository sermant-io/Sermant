package com.huawei.apm.core.lubanops.bootstrap.plugin.common;

import java.util.List;
import java.util.Map;

import com.huawei.apm.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.NonePrimaryKeyAggregator;

/**
 * 默认版本指标集聚合器
 */
public class DefaultVersionAggregator extends NonePrimaryKeyAggregator {
    private String version;

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    public MonitorDataRow constructItemRow() {
        MonitorDataRow row = new MonitorDataRow(1);
        if (version != null) {
            row.put("version", version);
        }
        return row;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public List<MonitorDataRow> getAllStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {

    }
}
