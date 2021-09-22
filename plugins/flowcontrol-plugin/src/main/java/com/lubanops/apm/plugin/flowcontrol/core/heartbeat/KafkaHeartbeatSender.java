/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.heartbeat;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.lubanops.apm.plugin.flowcontrol.core.config.ConfigConst;
import com.lubanops.apm.plugin.flowcontrol.core.util.KafkaProducerUtil;
import com.lubanops.apm.plugin.flowcontrol.core.util.PluginConfigUtil;

/**
 * 发送sentinel客户端心跳消息
 *
 * @author liyi
 * @since 2020-08-26
 */
public class KafkaHeartbeatSender {
    /**
     * 发送心跳消息
     */
    public void sendHeartbeat() {
        HeartbeatMessage heartbeatMessage = HeartbeatMessage.HEARTBEAT_SEND;
        String msg = heartbeatMessage.generateCurrentMessage();
        if (msg != null && !"".equals(msg)) {
            // 调用kafka发送消息
            RecordLog.info("[KafkaHeartbeatSender] heartbeat message=" + msg);
            KafkaProducerUtil.sendMessage(PluginConfigUtil.getValueByKey(ConfigConst.KAFKA_HEARTBEAT_TOPIC), msg);
        } else {
            // 对象转json字符串出错
            RecordLog.error("[KafkaHeartbeatSender] heartbeat json conversion error ");
        }
    }
}
