/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.test.mq.prohibition.kafka.controller;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

/**
 * KafkaConsumer接口
 *
 * @author lilai
 * @since 2024-01-09
 */
@RestController
public class KafkaConsumerController {
    private static final int POLL_DURATION = 2000;

    private static final int SLEEP_TIME = 3000;

    private static final String GROUP = "test";

    private static final String TOPIC = "test-topic";

    private static final int PARTITION_SUM = 2;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private KafkaAdminClient adminClient;

    private final Properties properties = new Properties();

    private KafkaConsumer<?, ?> consumer;

    @Value("${client.id}")
    private String clientId;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        adminClient = (KafkaAdminClient) AdminClient.create(properties);
    }

    /**
     * 创建消费者实例,订阅多个Topic
     */
    @GetMapping("/createConsumerWithMultiTopics")
    public void createConsumerWithMultiTopics() {
        createConsumer(Arrays.asList("test-topic-1", "test-topic-2"));
    }

    /**
     * 创建消费者实例，订阅1个Topic
     */
    @GetMapping("/createConsumerWithOneTopic")
    public void createConsumerWithOneTopic() {
        createConsumer(Collections.singletonList(TOPIC));
    }

    /**
     * 关闭消费者实例
     *
     * @throws InterruptedException 中断异常
     */
    @GetMapping("/closeConsumer")
    public void closeConsumer() throws InterruptedException {
        closed.set(true);
        Thread.sleep(SLEEP_TIME);
        closed.set(false);
    }

    /**
     * 获取消费者组中消费者数量
     *
     * @return 消费者数量
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     */
    @GetMapping(value = "/getConsumerGroupSize")
    public int getConsumerGroupSize() throws InterruptedException,
            ExecutionException {
        DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(
                Collections.singleton(GROUP));
        Map<String, ConsumerGroupDescription> descriptions = describeConsumerGroupsResult.all()
                .get();
        ConsumerGroupDescription groupDescription = descriptions.get(GROUP);
        if (groupDescription == null) {
            return 0;
        }
        return groupDescription.members().size();
    }

    /**
     * 获取消费者订阅的topic
     *
     * @return 消费者订阅的topic
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     */
    @GetMapping(value = "/getConsumerTopic")
    public Set<String> getConsumerGroupTopic() throws InterruptedException,
            ExecutionException {
        DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(
                Collections.singleton(GROUP));
        Map<String, ConsumerGroupDescription> descriptions = describeConsumerGroupsResult.all()
                .get();
        ConsumerGroupDescription groupDescription = descriptions.get(GROUP);
        if (groupDescription == null) {
            return Collections.emptySet();
        }
        for (MemberDescription description : groupDescription.members()) {
            if (description.clientId().equals(clientId)) {
                Set<String> topics = new HashSet<>();
                description.assignment().topicPartitions()
                        .forEach(topicPartition -> topics.add(topicPartition.topic()));
                return topics;
            }
        }
        return Collections.emptySet();
    }

    /**
     * 创建测试Topic
     */
    @GetMapping(value = "/createTopics")
    public void createTopic() {
        NewTopic firstTopic = new NewTopic(TOPIC, PARTITION_SUM, (short) 1);
        NewTopic secondTopic = new NewTopic("test-topic-1", 1, (short) 1);
        NewTopic thirdTopic = new NewTopic("test-topic-2", 1, (short) 1);
        adminClient.createTopics(Arrays.asList(firstTopic, secondTopic, thirdTopic));
    }

    /**
     * 测试服务是否正常启动
     *
     * @return String 响应
     */
    @GetMapping(value = "/healthCheck")
    public String healthCheck() {
        return "ok";
    }

    private void createConsumer(List<String> topics) {
        consumer = new KafkaConsumer<String, String>(properties);
        new Thread(() -> {
            try {
                consumer.subscribe(topics);
                while (!closed.get()) {
                    consumer.poll(POLL_DURATION);
                }
            } catch (WakeupException exception) {
                if (!closed.get()) {
                    throw exception;
                }
            } finally {
                consumer.close();
            }
        }).start();
    }
}
