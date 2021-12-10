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

package com.huawei.flowrecordreplay.console.rtc.consumer;

import com.huawei.flowrecordreplay.console.rtc.common.kafka.HeartbeatMessage;

import com.lmax.disruptor.dsl.Disruptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 针对心跳数据的disruptor生产者，起到的作用是将从kafka取到的数据二级缓存到disruptor的ringbuffer中
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Component("heartbeatProducer")
public class HeartbeatDisruptorProducer {
    /**
     * 针对心跳数据的disruptor的实例
     */
    @Autowired
    Disruptor<HeartbeatMessage> heartbeatDisruptor;

    /**
     * 该方法是将数据发送到ringbuffer中
     *
     * @param heartbeatMessages 参数表示从kafka取到的数据
     */
    public void send(List<HeartbeatMessage> heartbeatMessages) {
        /*
         * disruptor发布消息到ringbuffer
         *
         * @param event  into which the data should be translated.
         * @param sequence that is assigned to event.
         */
        for (HeartbeatMessage heartbeat : heartbeatMessages) {
            heartbeatDisruptor.publishEvent((event, seq) -> {
                event.setApp(heartbeat.getApp());
                event.setHostname(heartbeat.getHostname());
                event.setIp(heartbeat.getIp());
                event.setHeartbeatVersion(heartbeat.getHeartbeatVersion());
                event.setRetryTimes(heartbeat.getRetryTimes());
            });
        }
    }
}
