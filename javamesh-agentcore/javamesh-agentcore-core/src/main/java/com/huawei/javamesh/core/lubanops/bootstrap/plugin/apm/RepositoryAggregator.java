/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.bootstrap.plugin.apm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.AbstractAggregator;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;

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
