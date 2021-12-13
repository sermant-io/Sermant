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

package com.huawei.flowrecordreplay.console.rtc.consumer.strategy;

import com.huawei.flowrecordreplay.console.rtc.common.kafka.HeartbeatMessage;
import com.huawei.flowrecordreplay.console.rtc.common.redis.RedisUtil;
import com.huawei.flowrecordreplay.console.rtc.consumer.HeartbeatDisruptorProducer;

import com.alibaba.fastjson.JSONObject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 该类是ITopicHandleStrategy的实现类，专门用于处理从kafka拉取出来的心跳数据
 *
 * @author hanpeng
 * @since 2021-04-07
 */

@Component("topic-heartbeat")
public class HeartbeatHandleStrategy implements InterfaceTopicHandleStrategy {
    /**
     * redis工具类对象
     */
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    HeartbeatDisruptorProducer heartbeatDisruptorProducer;

    /**
     * 重写ITopicHandleStrategy的handleRecordByTopic()方法，用于处理从kafka拉取出来的心跳数据
     *
     * @param records 表示心跳数据集
     */
    @Override
    public void handleRecordByTopic(List<ConsumerRecord<String, String>> records) {
        if (records == null) {
            return;
        }
        List<HeartbeatMessage> heartbeatMessages = parseHeartbeatMassages(records);
        heartbeatDisruptorProducer.send(heartbeatMessages);
    }

    private List<HeartbeatMessage> parseHeartbeatMassages(List<ConsumerRecord<String, String>> records) {
        return records.parallelStream()
                .map(record -> JSONObject.parseObject(record.value(), HeartbeatMessage.class))
                .filter(Objects::nonNull)
                .filter(HeartbeatMessage::validate)
                .collect(Collectors.toList());
    }
}
