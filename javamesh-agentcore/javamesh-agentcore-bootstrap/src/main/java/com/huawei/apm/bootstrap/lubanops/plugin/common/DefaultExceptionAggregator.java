package com.huawei.apm.bootstrap.lubanops.plugin.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.huawei.apm.bootstrap.lubanops.collector.api.MonitorDataRow;
import com.huawei.apm.bootstrap.lubanops.collector.api.MultiPrimaryKeyAggregator;

/**
 * 默认异常指标集聚合器
 */
public class DefaultExceptionAggregator extends MultiPrimaryKeyAggregator<DefaultExceptionStats> {
    @Override
    public String getName() {
        return "exception";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> rows) {
        return null;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    protected Class<DefaultExceptionStats> getValueType() {
        return DefaultExceptionStats.class;
    }

    @Override
    protected List<String> primaryKey() {
        return Arrays.asList("exceptionType", "causeType");
    }

    public void onThrowable(Throwable t) {
        onThrowable(t, null);
    }

    @Override
    protected int primaryKeyLength() {
        return 2;
    }

    public void onThrowable(Throwable t, String content) {
        if (t == null) {
            return;
        }
        String type = t.getClass().getName();
        String cause;
        Throwable c = t.getCause();
        if (c == null) {
            cause = "NONE";
        } else {
            cause = c.getClass().getName();
        }
        DefaultExceptionStats v = this.getValue(type, cause);
        v.onThrowable(t, content);
    }
}
