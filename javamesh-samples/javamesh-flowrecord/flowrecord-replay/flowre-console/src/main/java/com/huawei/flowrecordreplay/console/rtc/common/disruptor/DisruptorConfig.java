/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.common.disruptor;

import com.huawei.flowrecordreplay.console.rtc.common.kafka.HeartbeatMessage;
import com.huawei.flowrecordreplay.console.rtc.consumer.HeartbeatMessageHandler;

import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disruptor的配置类，主要作用是初始化disruptor对象，并且针对不同的数据用不同的处理对象
 * <p>
 * 这里返回的主要有监控数据和心跳数据的disruptor对象
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Configuration
public class DisruptorConfig {
    private static final int HANDLER_NUM = 3;
    /**
     * disruptor中ringbuffer的大小
     */
    @Value("${disruptor.ringbuffer.size:2048}")
    int buffersize;

    /**
     * 针对ringbuffer中的心跳数据的处理类对象
     */
    @Autowired
    WorkHandler<HeartbeatMessage> heartbeatHandler;
    @Autowired
    ObjectFactory<HeartbeatMessageHandler> heartbeatFactory;

    /**
     * 往springboot中注入心跳数据的disruptor对象
     *
     * @return 返回一个disruptor对象
     */
    @Bean("heartbeatDisruptor")
    public Disruptor<HeartbeatMessage> disruptorHeartbeat() {
        Disruptor<HeartbeatMessage> disruptor = new Disruptor<>(HeartbeatMessage::new,
                buffersize, DaemonThreadFactory.INSTANCE);
        HeartbeatMessageHandler[] heartbeatMessageHandlers = new HeartbeatMessageHandler[HANDLER_NUM];
        for (int num = 0; num < heartbeatMessageHandlers.length; num++) {
            heartbeatMessageHandlers[num] = heartbeatFactory.getObject();
        }

        // 将心跳数据的处理类的对象和disruptor绑定
        disruptor.handleEventsWithWorkerPool(heartbeatMessageHandlers);
        disruptor.start();
        return disruptor;
    }
}
