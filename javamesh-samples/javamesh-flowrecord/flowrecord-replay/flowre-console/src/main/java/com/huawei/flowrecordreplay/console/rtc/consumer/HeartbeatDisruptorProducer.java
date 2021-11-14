/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
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
