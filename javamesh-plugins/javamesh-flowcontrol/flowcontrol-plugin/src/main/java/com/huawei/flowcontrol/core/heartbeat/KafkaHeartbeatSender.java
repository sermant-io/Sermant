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

package com.huawei.flowcontrol.core.heartbeat;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.util.KafkaProducerUtil;
import com.huawei.flowcontrol.core.util.PluginConfigUtil;

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
