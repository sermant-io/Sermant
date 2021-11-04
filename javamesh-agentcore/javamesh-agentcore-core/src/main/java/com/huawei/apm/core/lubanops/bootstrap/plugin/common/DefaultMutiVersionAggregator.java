package com.huawei.apm.core.lubanops.bootstrap.plugin.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.apm.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.NonePrimaryKeyAggregator;

/**
 * 默认多版本指标集聚合器
 */
public class DefaultMutiVersionAggregator extends NonePrimaryKeyAggregator {
    private Map<String, String> versionMap = new HashMap<String, String>();

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
        row.putAll(versionMap);
        return row;
    }

    public void setVersion(String jar, String version) {
        this.versionMap.put(jar, version);
    }

    @Override
    public List<MonitorDataRow> getAllStatus() {
        return null;
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    @Override
    public void clear() {

    }
}
