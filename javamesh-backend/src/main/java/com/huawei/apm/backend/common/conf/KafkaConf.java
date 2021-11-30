/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.backend.common.conf;

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

    // Log topic  name
    @Value("topic-log")
    private String topicLog;

    // 流控插件topic name
    @Value("topic-flowcontrol")
    private String topicFlowControl;

    // 录制插件topic name
    @Value("topic-flowecord")
    private String topicFlowRecord;

    @Value("${kafka.server-monitor.topic}")
    private String topicServerMonitor;

    @Value("${kafka.oracle-jvm-monitor.topic}")
    private String topicOracleJvmMonitor;

    @Value("${kafka.ibm-jvm-monitor.topic}")
    private String topicIbmJvmMonitor;

    @Value("${kafka.agent-registration.topic}")
    private String topicAgentRegistration;

    @Value("${kafka.druid-monitor.topic}")
    private String topicDruidMonitor;

    @Value("${kafka.agent-monitor.topic}")
    private String topicAgentMonitor;

    @Value("${kafka.agent-span-event.topic}")
    private String topicAgentSpanEvent;

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
}
