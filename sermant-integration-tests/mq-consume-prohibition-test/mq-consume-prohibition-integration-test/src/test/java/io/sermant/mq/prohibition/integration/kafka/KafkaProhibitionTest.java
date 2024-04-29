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

package io.sermant.mq.prohibition.integration.kafka;

import io.sermant.mq.prohibition.integration.utils.DynamicConfigUtils;
import io.sermant.mq.prohibition.integration.utils.HttpRequestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Kafka禁消费集成测试
 *
 * @author lilai
 * @since 2024-01-09
 */
@EnabledIfSystemProperty(named = "mq.consume.prohibition.test.type", matches = "KAFKA")
public class KafkaProhibitionTest {
    private static final String CONFIG_PATH = "/app=default&environment=&zone=hangzhou/sermant.mq.consume.globalConfig";

    private static final String TEST_CLIENT_1_ADDRESS = "http://127.0.0.1:7070";

    private static final String TEST_CLIENT_2_ADDRESS = "http://127.0.0.1:7071";

    private static final String CREATE_TOPICS_API = "/createTopics";

    private static final String CREATE_CONSUMER_WITH_ONE_TOPIC_API = "/createConsumerWithOneTopic";

    private static final String CREATE_CONSUMER_WITH_MULTI_TOPIC_API = "/createConsumerWithMultiTopics";

    private static final String GET_CONSUMER_GROUP_SIZE_API = "/getConsumerGroupSize";

    private static final String GET_CONSUMER_TOPIC_API = "/getConsumerTopic";

    private static final String CLOSE_CONSUMER_API = "/closeConsumer";

    private static final long WAIT_TIME = 2000;

    private static final long LONG_WAIT_TIME = 5000;

    private static final String lINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * 创建Topic
     *
     * @throws InterruptedException 中断异常
     */
    @BeforeAll
    public static void setUp() throws InterruptedException {
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_TOPICS_API);
        Thread.sleep(WAIT_TIME);
    }

    /**
     * 测试单个消费者订阅单个Topic，启动前关闭禁消费开关
     *
     * @throws Exception 异常
     */
    @Test
    public void testSingleConsumerSingleTopicStartUpWithSwitchOff() throws Exception {
        // Consumer启动前关闭禁止消费配置
        String config = "enableKafkaProhibition: false" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic";
        DynamicConfigUtils.updateConfig(CONFIG_PATH, config);
        // 创建单Topic的消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_CONSUMER_WITH_ONE_TOPIC_API);
        Thread.sleep(WAIT_TIME);
        int groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
        String topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
        try {
            // 启动前关闭禁止消费的测试
            Assertions.assertEquals(1, groupSize);
            Assertions.assertTrue(topics.contains("test-topic"));
        } finally {
            // 清理资源，关闭消费者test-client-1以及删除配置
            HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CLOSE_CONSUMER_API);
            clearConfig();
            Thread.sleep(WAIT_TIME);
        }
    }

    /**
     * 测试单个消费者订阅多个Topic，启动前关闭禁消费开关
     *
     * @throws Exception 异常
     */
    @Test
    public void testSingleConsumerMultiTopicStartUpWithSwitchOff() throws Exception {
        // Consumer启动前关闭禁止消费配置
        String config = "enableKafkaProhibition: false" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic-1" + lINE_SEPARATOR +
                "  - test-topic-2";
        DynamicConfigUtils.updateConfig(CONFIG_PATH, config);
        // 创建单Topic的消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_CONSUMER_WITH_MULTI_TOPIC_API);
        Thread.sleep(WAIT_TIME);
        int groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
        String topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
        try {
            // 启动前关闭禁止消费的测试
            Assertions.assertEquals(1, groupSize);
            Assertions.assertTrue(topics.contains("test-topic-1"));
            Assertions.assertTrue(topics.contains("test-topic-2"));
        } finally {
            // 清理资源，关闭消费者test-client-1以及删除配置
            HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CLOSE_CONSUMER_API);
            clearConfig();
            Thread.sleep(WAIT_TIME);
        }
    }

    /**
     * 测试单个消费者订阅单个Topic，启动前开启禁消费开关，运行时恢复消费，运行时开启禁消费开关
     *
     * @throws Exception 异常
     */
    @Test
    public void testSingleConsumerSingleTopic() throws Exception {
        // Consumer启动前开启禁止消费配置
        String configOn = "enableKafkaProhibition: true" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic";
        DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
        // 创建单Topic的消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_CONSUMER_WITH_ONE_TOPIC_API);
        Thread.sleep(WAIT_TIME);
        int groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
        String topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
        try {
            // 启动前开启禁止消费的测试
            Assertions.assertEquals(0, groupSize);
            Assertions.assertEquals("[]", topics);
            // 运行时下发恢复消费的配置
            String configOff = "enableKafkaProhibition: false" + lINE_SEPARATOR +
                    "kafkaTopics:" + lINE_SEPARATOR +
                    "  - test-topic";
            DynamicConfigUtils.updateConfig(CONFIG_PATH, configOff);
            Thread.sleep(WAIT_TIME);
            groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
            topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
            // 运行时恢复消费的测试
            Assertions.assertEquals(1, groupSize);
            Assertions.assertTrue(topics.contains("test-topic"));
            // 运行时下发开启禁止消费的配置
            DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
            Thread.sleep(WAIT_TIME);
            groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
            topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
            Assertions.assertEquals(0, groupSize);
            Assertions.assertEquals("[]", topics);
        } finally {
            // 清理资源，关闭消费者test-client-1以及删除配置
            HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CLOSE_CONSUMER_API);
            Thread.sleep(WAIT_TIME);
            clearConfig();
        }
    }

    /**
     * 测试单个消费者订阅多个Topic，启动前开启全部Topic的禁消费开关，运行时恢复消费，运行时开启禁消费开关
     *
     * @throws Exception 异常
     */
    @Test
    public void testSingleConsumerMultiTopicsWithAllTopicsToProhibit() throws Exception {
        // Consumer启动前开启禁止消费所有Topic的配置
        String configOn = "enableKafkaProhibition: true" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic-1" + lINE_SEPARATOR +
                "  - test-topic-2";
        DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
        // 创建多Topic的消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_CONSUMER_WITH_MULTI_TOPIC_API);
        Thread.sleep(WAIT_TIME);
        int groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
        String topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
        try {
            // 启动前开启禁止消费的测试
            Assertions.assertEquals(0, groupSize);
            Assertions.assertEquals("[]", topics);
            // 运行时下发恢复消费所有Topic的配置
            String configOff = "enableKafkaProhibition: false" + lINE_SEPARATOR +
                    "kafkaTopics:" + lINE_SEPARATOR +
                    "  - test-topic-1" + lINE_SEPARATOR +
                    "  - test-topic-2";
            DynamicConfigUtils.updateConfig(CONFIG_PATH, configOff);
            Thread.sleep(WAIT_TIME);
            groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
            topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
            // 运行时恢复消费所有Topic的测试
            Assertions.assertEquals(1, groupSize);
            Assertions.assertTrue(topics.contains("test-topic-1"));
            Assertions.assertTrue(topics.contains("test-topic-2"));
            // 运行时下发开启禁止消费所有Topic的配置
            DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
            Thread.sleep(WAIT_TIME);
            groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
            topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
            Assertions.assertEquals(0, groupSize);
            Assertions.assertEquals("[]", topics);
        } finally {
            // 清理资源，关闭消费者test-client-1以及删除配置
            HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CLOSE_CONSUMER_API);
            Thread.sleep(WAIT_TIME);
            clearConfig();
        }
    }

    /**
     * 测试单个消费者订阅多个Topic，启动前开启单个Topic的禁消费开关，运行时恢复消费，运行时开启禁消费开关
     *
     * @throws Exception 异常
     */
    @Test
    public void testSingleConsumerMultiTopicsWithOneTopicToProhibit() throws Exception {
        // Consumer启动前开启禁止消费所有Topic的配置
        String configOn = "enableKafkaProhibition: true" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic-1";
        DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
        // 创建多Topic的消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_CONSUMER_WITH_MULTI_TOPIC_API);
        Thread.sleep(WAIT_TIME);
        int groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
        String topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
        try {
            // 启动前开启禁止消费的测试
            Assertions.assertEquals(1, groupSize);
            Assertions.assertFalse(topics.contains("test-topic-1"));
            Assertions.assertTrue(topics.contains("test-topic-2"));
            // 运行时下发恢复消费所有Topic的配置
            String configOff = "enableKafkaProhibition: false" + lINE_SEPARATOR +
                    "kafkaTopics:" + lINE_SEPARATOR +
                    "  - test-topic-1";
            DynamicConfigUtils.updateConfig(CONFIG_PATH, configOff);
            Thread.sleep(LONG_WAIT_TIME);
            groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
            topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
            // 运行时恢复消费所有Topic的测试
            Assertions.assertEquals(1, groupSize);
            Assertions.assertTrue(topics.contains("test-topic-1"));
            Assertions.assertTrue(topics.contains("test-topic-2"));
            // 运行时下发开启禁止消费所有Topic的配置
            DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
            Thread.sleep(LONG_WAIT_TIME);
            groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
            topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
            Assertions.assertEquals(1, groupSize);
            Assertions.assertFalse(topics.contains("test-topic-1"));
            Assertions.assertTrue(topics.contains("test-topic-2"));
        } finally {
            // 清理资源，关闭消费者test-client-1以及删除配置
            HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CLOSE_CONSUMER_API);
            Thread.sleep(WAIT_TIME);
            clearConfig();
        }
    }

    /**
     * 测试两个消费者订阅同一个Topic，启动前开启其中一个消费者的禁消费开关，运行时恢复消费，运行时开启禁消费开关
     *
     * @throws Exception 异常
     */
    @Test
    public void testMultiConsumersOneTopicWithOneConsumerToProhibit() throws Exception {
        // Consumer启动前开启禁止消费的配置
        String configOn = "enableKafkaProhibition: true" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic";
        DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
        // 创建单Topic的消费者test-client-1,test-client-2
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_CONSUMER_WITH_ONE_TOPIC_API);
        HttpRequestUtils.doGet(TEST_CLIENT_2_ADDRESS + CREATE_CONSUMER_WITH_ONE_TOPIC_API);
        Thread.sleep(WAIT_TIME);
        int groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
        String topicsForFirstClient;
        String topicsForSecondClient = HttpRequestUtils.doGet(TEST_CLIENT_2_ADDRESS + GET_CONSUMER_TOPIC_API);
        try {
            // 启动前开启禁止消费的测试
            Assertions.assertEquals(1, groupSize);
            Assertions.assertTrue(topicsForSecondClient.contains("test-topic"));
            // 运行时下发恢复消费的配置
            String configOff = "enableKafkaProhibition: false" + lINE_SEPARATOR +
                    "kafkaTopics:" + lINE_SEPARATOR +
                    "  - test-topic";
            DynamicConfigUtils.updateConfig(CONFIG_PATH, configOff);
            Thread.sleep(LONG_WAIT_TIME);
            groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
            topicsForFirstClient = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
            topicsForSecondClient = HttpRequestUtils.doGet(TEST_CLIENT_2_ADDRESS + GET_CONSUMER_TOPIC_API);
            // 运行时恢复消费的测试
            Assertions.assertEquals(2, groupSize);
            Assertions.assertTrue(topicsForFirstClient.contains("test-topic"));
            Assertions.assertTrue(topicsForSecondClient.contains("test-topic"));
            // 运行时下发开启禁止消费所有Topic的配置
            DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
            Thread.sleep(LONG_WAIT_TIME);
            groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
            topicsForSecondClient = HttpRequestUtils.doGet(TEST_CLIENT_2_ADDRESS + GET_CONSUMER_TOPIC_API);
            Assertions.assertEquals(1, groupSize);
            Assertions.assertTrue(topicsForSecondClient.contains("test-topic"));
        } finally {
            // 清理资源，关闭消费者test-client-1、test-client-2以及删除配置
            HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CLOSE_CONSUMER_API);
            HttpRequestUtils.doGet(TEST_CLIENT_2_ADDRESS + CLOSE_CONSUMER_API);
            Thread.sleep(WAIT_TIME);
            clearConfig();
        }
    }

    /**
     * 测试单个消费者订阅单个Topic，开启禁消费前消费者shutdown
     *
     * @throws Exception 异常
     */
    @Test
    public void testSingleConsumerShutDownBeforeSwitchOn() throws Exception {
        // 创建单Topic的消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_CONSUMER_WITH_ONE_TOPIC_API);
        Thread.sleep(WAIT_TIME);
        // 关闭消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CLOSE_CONSUMER_API);
        // 开启禁止消费配置
        String configOn = "enableKafkaProhibition: true" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic";
        DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
        int groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
        String topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
        try {
            Assertions.assertEquals(0, groupSize);
            Assertions.assertEquals("[]", topics);
        } finally {
            // 清理资源
            Thread.sleep(WAIT_TIME);
            clearConfig();
        }
    }

    /**
     * 测试单个消费者订阅单个Topic，恢复禁消费前消费者shutdown
     *
     * @throws Exception 异常
     */
    @Test
    public void testSingleConsumerShutDownBeforeSwitchOff() throws Exception {
        // 创建单Topic的消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CREATE_CONSUMER_WITH_ONE_TOPIC_API);
        Thread.sleep(WAIT_TIME);
        // 开启禁止消费配置
        String configOn = "enableKafkaProhibition: true" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic";
        DynamicConfigUtils.updateConfig(CONFIG_PATH, configOn);
        // 关闭消费者test-client-1
        HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + CLOSE_CONSUMER_API);
        // 关闭禁止消费配置
        String configOff = "enableKafkaProhibition: false" + lINE_SEPARATOR +
                "kafkaTopics:" + lINE_SEPARATOR +
                "  - test-topic";
        int groupSize = Integer.parseInt(HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_GROUP_SIZE_API));
        String topics = HttpRequestUtils.doGet(TEST_CLIENT_1_ADDRESS + GET_CONSUMER_TOPIC_API);
        DynamicConfigUtils.updateConfig(CONFIG_PATH, configOff);
        try {
            Assertions.assertEquals(0, groupSize);
            Assertions.assertEquals("[]", topics);
        } finally {
            // 清理资源
            Thread.sleep(WAIT_TIME);
            clearConfig();
        }
    }

    /**
     * 清除禁消费配置
     *
     * @throws Exception 异常
     */
    private void clearConfig() throws Exception {
        String config = "enableKafkaProhibition: false";
        DynamicConfigUtils.updateConfig("/app=default&environment=&zone=hangzhou/sermant.mq.consume.globalConfig",
                config);
    }
}
