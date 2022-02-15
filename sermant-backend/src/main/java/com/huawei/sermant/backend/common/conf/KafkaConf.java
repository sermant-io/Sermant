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

package com.huawei.sermant.backend.common.conf;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * kafka的配置类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
@Getter
@Setter
@Component
@Configuration
public class KafkaConf {
    // kafka地址
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServers;

    // common主题名
    @Value("${kafka.heartbeat.topic}")
    private String topicHeartBeat;

    @Value("${kafka.pool.timeoutMs}")
    private Integer kafkaPoolTimeoutMs;

    @Value("${kafka.key.deserializer}")
    private String kafkaKeyDeserializer;

    @Value("${kafka.value.deserializer}")
    private String kafkaValueDeserializer;

    @Value("${kafka.group.id}")
    private String kafkaGroupId;

    @Value("${kafka.enable.auto.commit}")
    private String kafkaEnableAutoCommit;

    @Value("${kafka.auto.commit.interval.ms}")
    private String kafkaAutoCommitIntervalMs;

    @Value("${kafka.auto.offset.reset}")
    private String kafkaAutoOffsetReset;

    @Value("${kafka.session.timeout.ms}")
    private String kafkaSessionTimeoutMs;

    @Value("${fetch.min.bytes}")
    private String kafkaFetchMinBytes;

    @Value("${fetch.max.wait.ms}")
    private String kafkaFetchMaxWaitMs;

    @Value("${kafka.key.serializer}")
    private String kafkaKeySerializer;

    @Value("${kafka.value.serializer}")
    private String kafkaValueSerializer;

    @Value("${kafka.acks}")
    private String kafkaAcks;

    @Value("${kafka.max.request.size}")
    private String kafkaMaxRequestSize;

    @Value("${kafka.buffer.memory}")
    private String kafkaBufferMemory;

    @Value("${kafka.retries}")
    private String kafkaRetries;

    @Value("${kafka.request.timeout.ms}")
    private String kafkaRequestTimeoutMs;

    @Value("${kafka.max.block.ms}")
    private String kafkaMaxBlockMs;

    @Value("${heartbeat.cache}")
    private String isHeartbeatCache;
}
