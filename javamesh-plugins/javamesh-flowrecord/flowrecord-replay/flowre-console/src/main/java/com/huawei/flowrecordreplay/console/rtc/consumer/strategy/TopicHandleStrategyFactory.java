/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.rtc.consumer.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 专门用作针对kafka中的数据进行处理的策略工厂
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Component
public class TopicHandleStrategyFactory {
    /**
     * 该map存的是各种kafka中数据的处理策略类
     * <p>
     * map中的
     * key：存的是处理策略类的bean名
     * value：存的是处理策略类的对象
     */
    @Autowired
    private final Map<String, InterfaceTopicHandleStrategy> topicHandleStrategyMap = new ConcurrentHashMap<>(16);

    /**
     * 用于根据topic名称获取对应的处理策略对象
     *
     * @param topic topic名称
     * @return 返回具体的策略类对象实例
     * @throws RuntimeException no topicHandleStrategy defined
     */
    public InterfaceTopicHandleStrategy getTopicHandleStrategy(String topic) throws IllegalArgumentException {
        InterfaceTopicHandleStrategy topicHandleStrategy = topicHandleStrategyMap.get(topic);
        if (topicHandleStrategy == null) {
            throw new IllegalArgumentException("no topicHandleStrategy defined");
        }
        return topicHandleStrategy;
    }
}
