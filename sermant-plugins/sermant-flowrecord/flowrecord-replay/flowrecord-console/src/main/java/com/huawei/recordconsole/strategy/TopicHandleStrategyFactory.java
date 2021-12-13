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

package com.huawei.recordconsole.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 专门用作针对kafka中的数据进行处理的策略工厂
 *
 * @author lihongjiang
 * @since 2021-02-19
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
