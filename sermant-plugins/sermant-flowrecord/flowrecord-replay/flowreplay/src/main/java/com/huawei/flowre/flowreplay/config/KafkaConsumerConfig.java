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

package com.huawei.flowre.flowreplay.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * kafka consumer properties
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-30
 */
@Configuration
public class KafkaConsumerConfig {
    /**
     * kafka服务器地址
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    /**
     * 组id
     */
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * 在kafka中每当消费者查找不到所记录的消费位移 时， 就会根据消费者客户端参数
     * auto.offset.reset 的配置来决定从何处开始进行消费，这个参数的默认值为“latest ”，表
     * 示从分区末尾开始消费消息。如果将 auto.offset.reset参数配置为“earliest”，那么消费者会从起始处，也就是0开始消费。
     */
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    /**
     * Kafka 中默认的消费位移的提交方式是自动提交，这个由消费者客户端参数
     * enable.auto.commit 配置，默认值为 true 。当然这个默认的自动提交不是每消费 条消息
     * 就提交 次，而是定期提交，这个定期的周期时间由客户端参数 auto.commit.interval.ms
     * 配置，默认值为5秒，此参数生效的前提是 enable.auto.commit参数为 true。
     */
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private boolean isEnableAutoCommit;

    /**
     * 消费者定时提交offset的时间周期，详情如上
     */
    @Value("${spring.kafka.consumer.auto-commit-interval}")
    private int autoCommitInterval;

    /**
     * 表示事务的隔离级别
     */
    @Value("${spring.kafka.consumer.properties.isolation.level}")
    private String isolationLevel;

    /**
     * Kafka 中有两个内部的主题：_consumer_offsets和__transaction_state。 exclude.internal.topics
     * 用来指定 Kafka 中的内部主题是否可以向消费者公开，默认值为 true 。如果设置 true ，那么只
     * 能使用 subscribe( Collection）的方式而不能使用 subscribe(Pattern）的方式来订阅内部主题，设置为
     * false 则没有这个限制。
     */
    @Value("${spring.kafka.consumer.properties.exclude.internal.topics}")
    private boolean isExcludeInternalTopics;

    /**
     * max.poll.records <= 吞吐量
     * 单次poll调用返回的最大消息数，如果处理逻辑很轻量，可以适当提高该值。
     * 一次从kafka中poll出来的数据条数,max.poll.records条数据需要在在session.timeout.ms这个时间内处理完
     * 默认值为500
     */
    @Value("${spring.kafka.consumer.max-poll-records}")
    private String maxPollRecords;

    /**
     * 处理逻辑最大时间
     */
    @Value("${spring.kafka.consumer.properties.max.poll.interval.ms}")
    private String maxPollIntervalMsConfig;

    /**
     * 默认值是10s
     * 该参数是 Consumer Group 主动检测 (组内成员comsummer)崩溃的时间间隔。若设置10min，
     * 那么Consumer Group的管理者（group coordinator）可能需要10分钟才能感受到。太漫长了是吧。
     */
    @Value("${spring.kafka.consumer.properties.session.timeout.ms}")
    private String sessionTimeoutMsConfig;

    @Value("${spring.kafka.consumer.properties.request.timeout.ms}")
    private String requestTimeoutMs;

    /**
     * 该方法主要是用于获取配置信息，整体用properties存起来
     *
     * @return 返回一个properties对象
     */
    @Bean
    public Properties kafkaConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, isEnableAutoCommit);
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMsConfig);
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMsConfig);
        properties.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolationLevel);
        properties.put(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG, isExcludeInternalTopics);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return properties;
    }
}
