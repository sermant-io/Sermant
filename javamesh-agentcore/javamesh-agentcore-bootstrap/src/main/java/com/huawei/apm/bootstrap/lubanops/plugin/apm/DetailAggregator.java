package com.huawei.apm.bootstrap.lubanops.plugin.apm;

import java.util.List;
import java.util.Map;

import com.huawei.apm.bootstrap.lubanops.collector.api.MonitorDataRow;
import com.huawei.apm.bootstrap.lubanops.collector.api.SinglePrimaryKeyAggregator;

public class DetailAggregator extends SinglePrimaryKeyAggregator<APMStats> {

    public static final String AGGREGATE_KEY_TYPE = "type";

    public static final String AGGREAGET_METRIC_SENDCOUNT = "sendCount";

    public static final String AGGREAGET_METRIC_DISCARDCOUNT = "discardCount";

    public static final String AGGREAGET_METRIC_ERRORCOUNT = "errorCount";

    public static final String AGGREAGET_METRIC_MAXBYTES = "maxBytes";

    public static final String AGGREAGET_METRIC_SENDBYTES = "sendBytes";

    public static final String AGGREAGET_METRIC_DISCARDBYTES = "discardBytes";

    public static final String AGGREAGET_METRIC_ERRORBYTES = "errorBytes";

    public static final String AGGREAGET_METRIC_MAXQUEUESIZE = "maxQueueSize";

    public static final String AGGREAGET_METRIC_SENDTOTALTIME = "sendTotalTime";

    public static final String AGGREAGET_METRIC_SLOWTIME = "slowTime";

    @Override
    protected Class<APMStats> getValueType() {
        return APMStats.class;
    }

    public void onStart(String type, int queueSize) {
        APMStats value = getValue(type);
        value.onStart(queueSize);
    }

    public void onDiscard(String type, long bytes) {
        APMStats value = getValue(type);
        value.onDiscard(bytes);
    }

    public void onThrowable(String type, long bytes) {
        APMStats value = getValue(type);
        value.onThrowable(bytes);
    }

    public void onFinally(String type, long useTime) {
        APMStats value = getValue(type);
        value.onFinally(useTime);
    }

    public void onSuccess(String dataType, long bytes) {
        APMStats value = getValue(dataType);
        value.onSuccess(bytes);
    }

    @Override
    protected String primaryKey() {
        return AGGREGATE_KEY_TYPE;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    @Override
    public String getName() {
        return "detail";
    }

    @Override
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected) {
        return null;
    }

    public double getSendSuccessPercent(String type) {
        return getValue(type).getSuccessPercent();
    }

    public long getErrorCount(String type) {
        return getValue(type).getErrorCount();
    }

    public double getSendCount(String type) {
        return getValue(type).getSendCount();
    }
}
