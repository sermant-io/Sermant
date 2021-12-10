/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.core.metric;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.core.metric.provider.DefaultMetricProvider;
import com.huawei.flowcontrol.core.metric.provider.MetricProvider;

/**
 * 抽象发送器
 *
 * @author zhouss
 * @since 2021-12-07
 */
public abstract class AbstractMetricSender implements MetricSender {
    private final MetricProvider metricProvider = new DefaultMetricProvider();

    /**
     * 使用默认指标数据生成器发送数据
     */
    @Override
    public void sendMetric() {
        final Object data = metricProvider.buildMetric();
        if (data == null) {
            return;
        }
        RecordLog.debug("[MetricSender] metric message=" + JSONObject.toJSONString(data));
        sendMetric(data);
    }

    @Override
    public void sendMetric(MetricProvider metricProvider) {
        if (metricProvider == null) {
            sendMetric();
        } else {
            final Object data = metricProvider.buildMetric();
            if (data == null) {
                return;
            }
            RecordLog.debug("[MetricSender] metric message=" + JSONObject.toJSONString(data));
            sendMetric(data);
        }
    }
}
