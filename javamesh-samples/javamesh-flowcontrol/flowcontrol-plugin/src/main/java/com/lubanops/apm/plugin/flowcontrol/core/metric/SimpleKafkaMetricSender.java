/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.metric;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.lubanops.apm.plugin.flowcontrol.core.config.ConfigConst;
import com.lubanops.apm.plugin.flowcontrol.core.util.KafkaProducerUtil;
import com.lubanops.apm.plugin.flowcontrol.core.util.PluginConfigUtil;

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
