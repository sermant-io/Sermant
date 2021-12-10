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

import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.SinglePrimaryKeyAggregator;

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
