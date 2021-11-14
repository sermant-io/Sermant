package com.huawei.apm.core.lubanops.bootstrap.plugin.apm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.huawei.apm.core.lubanops.bootstrap.collector.api.AbstractAggregator;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.MonitorDataRow;

/**
 * @author
 */
public class RepositoryAggregator extends AbstractAggregator {

    public static final String METRIC_SET_NAME = "repository";

    public static final String AGGREGATE_METRIC_MONITOR_QUEUE_SIZE = "monitorQueueSize";

    public static final String AGGREGATE_METRIC_MONITOR_QUEUE_BYTE_SIZE = "monitorObjectSize";

    public static final String AGGREGATE_METRIC_TRACE_QUEUE_SIZE = "traceQueueSize";

    public static final String AGGREGATE_METRIC_TRACE_QUEUE_BYTE_SIZE = "traceObjectSize";

    private long monitorQueueSize;

    private long monitorObjectSize;

    private long traceQueueSize;

    private long traceObjectSize;

    @Override
    public void clear() {
    }

    @Override
    public String getName() {
        return METRIC_SET_NAME;
    }

    @Override
    public List<MonitorDataRow> harvest() {
        List<MonitorDataRow> result = new ArrayList<MonitorDataRow>();
        MonitorDataRow row = new MonitorDataRow();
        row.put(AGGREGATE_METRIC_MONITOR_QUEUE_SIZE, monitorQueueSize);
        row.put(AGGREGATE_METRIC_MONITOR_QUEUE_BYTE_SIZE, monitorObjectSize);
        row.put(AGGREGATE_METRIC_TRACE_QUEUE_SIZE, traceQueueSize);
        row.put(AGGREGATE_METRIC_TRACE_QUEUE_BYTE_SIZE, traceObjectSize);
        result.add(row);
        return result;
    }

    @Override
    public void parseParameters(Map<String, String> parameters) {

    }

    public void monitorQueueSize(long monitorQueueSize, long monitorObjectSize) {
        this.monitorObjectSize = monitorObjectSize;
        this.monitorQueueSize = monitorQueueSize;
    }

    public void traceQueueSize(long traceQueueSize, long traceObjectSize) {
        this.traceObjectSize = traceObjectSize;
        this.traceQueueSize = traceQueueSize;
    }
}
