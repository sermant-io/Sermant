package com.lubanops.apm.bootstrap.collector.api;

import java.util.Map;

import com.lubanops.apm.bootstrap.exception.ApmRuntimeException;

/**
 * 对于只有单个主键的采集器的基类
 * @param <T>
 * @author frank.yef
 */
public abstract class SinglePrimaryKeyAggregator<T extends StatsBase>
        extends AbstractPrimaryKeyValueAggregator<String, T> {

    /**
     * 默认主键
     */
    protected String defaultKey() {
        return DEFAULT_KEY;
    }

    @Override
    public String getPrimaryKey(Map<String, String> primaryKeyMap) {
        String key = primaryKeyMap.get(primaryKey());
        if (key == null) {
            throw new ApmRuntimeException("Key " + primaryKey() + " is null");
        }
        return key;
    }

    @Override
    protected void setPrimaryKey(MonitorDataRow row, String key) {
        row.put(primaryKey(), key);
    }

    /**
     * 主键
     */
    protected abstract String primaryKey();
}
