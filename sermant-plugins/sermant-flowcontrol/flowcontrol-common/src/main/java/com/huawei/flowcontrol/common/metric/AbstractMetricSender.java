/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.metric;

import com.huawei.flowcontrol.common.metric.provider.MetricProvider;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;

import com.alibaba.fastjson.JSONObject;

import java.util.logging.Logger;

/**
 * 抽象发送器
 *
 * @author zhouss
 * @since 2021-12-07
 */
public abstract class AbstractMetricSender implements MetricSender {
    private static final Logger LOGGER = LogFactory.getLogger();

    @Override
    public void sendMetric(MetricProvider metricProvider) {
        if (metricProvider == null) {
            return;
        }
        final Object data = metricProvider.buildMetric();
        if (data == null) {
            return;
        }
        LOGGER.fine("[MetricSender] metric message=" + JSONObject.toJSONString(data));
        sendMetric(data);
    }
}
