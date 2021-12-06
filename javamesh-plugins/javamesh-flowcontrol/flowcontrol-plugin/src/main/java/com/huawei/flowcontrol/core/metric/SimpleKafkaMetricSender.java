/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowcontrol.core.metric;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.util.KafkaProducerUtil;
import com.huawei.flowcontrol.core.util.PluginConfigUtil;

/**
 * 发送sentinel客户端流控数据消息
 *
 * @author liyi
 * @since 2020-08-26
 */
public class SimpleKafkaMetricSender {
    private long startTime;

    public long metricInitialDurationMs;

    public SimpleKafkaMetricSender() {
        metricInitialDurationMs = Long.parseLong(PluginConfigUtil.getValueByKey(ConfigConst.METRIC_INITIAL_DURATION));
        startTime = TimeUtil.currentTimeMillis();
    }

    /**
     * 发送metric消息到kafka broker
     *
     */
    public synchronized void sendMetric() {
        long endTime = TimeUtil.currentTimeMillis();
        // 查询监控数据
        String msg = MetricMessage.generateCurrentMessage(startTime, endTime);
        if (msg != null && !"".equals(msg)) {
            RecordLog.info("[SimpleKafkaMetricSender] metric message=" + msg);

            // 调用kafka发送消息
            KafkaProducerUtil.sendMessage(PluginConfigUtil.getValueByKey(ConfigConst.KAFKA_METRIC_TOPIC), msg);
        } else {
            return;
        }
        // 下一次的开始时间为本次查询的结束时间，
        startTime = endTime;
    }
}
