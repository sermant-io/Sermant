/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.mq.prohibition.controller.rocketmq;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.mq.prohibition.controller.rocketmq.cache.RocketMqConsumerCache;
import io.sermant.mq.prohibition.controller.rocketmq.constant.SubscriptionType;
import io.sermant.mq.prohibition.controller.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import io.sermant.mq.prohibition.controller.utils.RocketMqWrapperUtils;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.AssignedMessageQueue;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RocketMqPullConsumerController Unit test
 *
 * @author daizhenyu
 * @since 2023-12-26
 **/
public class RocketMqPullConsumerControllerTest {
    private static DefaultLitePullConsumer pullConsumer;

    private static DefaultLitePullConsumerWrapper pullConsumerWrapper;

    private static AssignedMessageQueue assignedMessageQueue;

    private static Set<String> prohibitionTopics;

    private static Set<String> subscribedTopics;

    @BeforeClass
    public static void setUp() throws MQClientException {
        MockedStatic<ConfigManager> configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());

        pullConsumer = Mockito.mock(DefaultLitePullConsumer.class);
        DefaultLitePullConsumerImpl pullConsumerImpl = Mockito.mock(DefaultLitePullConsumerImpl.class);
        RebalanceImpl rebalance = Mockito.mock(RebalanceImpl.class);
        MQClientInstance instance = Mockito.mock(MQClientInstance.class);
        assignedMessageQueue = Mockito.mock(AssignedMessageQueue.class);
        Mockito.when(instance.getClientConfig()).thenReturn(createClientConfig());
        Mockito.when(rebalance.getSubscriptionInner()).thenReturn(new ConcurrentHashMap<>());

        pullConsumerWrapper = new DefaultLitePullConsumerWrapper(pullConsumer, pullConsumerImpl, rebalance, instance);
        pullConsumerWrapper.setConsumerGroup("test-group");

        MockedStatic<RocketMqWrapperUtils> wrapperUtilsMockedStatic = Mockito
                .mockStatic(RocketMqWrapperUtils.class);
        wrapperUtilsMockedStatic.when(() -> RocketMqWrapperUtils.wrapPullConsumer(pullConsumer))
                .thenReturn(Optional.of(pullConsumerWrapper));

        prohibitionTopics = new HashSet<>();
        prohibitionTopics.add("test-topic-1");
    }

    /**
     * Consumer subscription topic is empty
     */
    @Test
    public void testDisablePullConsumptionNoTopic() {
        pullConsumerWrapper.setProhibition(true);
        pullConsumerWrapper.setSubscribedTopics(new HashSet<>());
        pullConsumerWrapper.setSubscriptionType(SubscriptionType.SUBSCRIBE);
        RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper, prohibitionTopics);
        Assert.assertFalse(pullConsumerWrapper.isProhibition());
    }

    /**
     * Consumer subscription topics intersect with prohibited consumption topics
     */
    @Test
    public void testDisablePullConsumptionWithSubTractTopics() {
        subscribedTopics = new HashSet<>();
        subscribedTopics.add("test-topic-1");
        subscribedTopics.add("test-topic-2");
        pullConsumerWrapper.setSubscribedTopics(subscribedTopics);
        pullConsumerWrapper.setProhibition(false);
        pullConsumerWrapper.setSubscriptionType(SubscriptionType.SUBSCRIBE);
        RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper, prohibitionTopics);
        Assert.assertTrue(pullConsumerWrapper.isProhibition());

        // 禁消费后，再次下发禁消费
        MQClientInstance clientFactory = pullConsumerWrapper.getClientFactory();
        Mockito.reset(clientFactory);
        RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper, prohibitionTopics);
        Mockito.verify(clientFactory, Mockito.times(0))
                .unregisterConsumer("test-group");
    }

    /**
     * There is no intersection between consumer subscription topics and prohibited consumption topics
     */
    @Test
    public void testDisablePullConsumptionWithNoSubTractTopics() {
        subscribedTopics = new HashSet<>();
        subscribedTopics.add("test-topic-2");
        subscribedTopics.add("test-topic-3");
        pullConsumerWrapper.setSubscribedTopics(subscribedTopics);
        pullConsumerWrapper.setProhibition(true);
        pullConsumerWrapper.setSubscriptionType(SubscriptionType.SUBSCRIBE);
        RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper, prohibitionTopics);
        Assert.assertFalse(pullConsumerWrapper.isProhibition());

        // 恢复消费后，再次下发禁消费配置
        MQClientInstance clientFactory = pullConsumerWrapper.getClientFactory();
        Mockito.reset(clientFactory);
        RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper, prohibitionTopics);
        Mockito.verify(clientFactory, Mockito.times(0))
                .registerConsumer(Mockito.any(), Mockito.any());
    }

    /**
     * Consumers specify a queue for consumption, and there is an intersection between the topics in the queue
     * and those prohibited from consumption
     */
    @Test
    public void testDisablePullConsumptionWithAssignSubTractTopics() {
        subscribedTopics = new HashSet<>();
        subscribedTopics.add("test-topic-1");
        subscribedTopics.add("test-topic-2");
        pullConsumerWrapper.setSubscribedTopics(subscribedTopics);
        pullConsumerWrapper.setSubscriptionType(SubscriptionType.ASSIGN);
        pullConsumerWrapper.setAssignedMessageQueue(assignedMessageQueue);
        RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper, prohibitionTopics);
        Mockito.verify(assignedMessageQueue, Mockito.times(1)).updateAssignedMessageQueue(
                Mockito.any());
    }

    /**
     * The consumer specifies a queue for consumption, and there is no intersection between the topics in the
     * queue and those prohibited from consumption
     */
    @Test
    public void testDisablePullConsumptionWithAssignNoSubTractTopics() {
        subscribedTopics = new HashSet<>();
        subscribedTopics.add("test-topic-2");
        Collection<MessageQueue> messageQueues = new ArrayList<>();
        MessageQueue messageQueue = new MessageQueue("test-topic-2", "broker-1", 1);
        messageQueues.add(messageQueue);
        pullConsumerWrapper.setMessageQueues(messageQueues);
        pullConsumerWrapper.setSubscribedTopics(subscribedTopics);
        pullConsumerWrapper.setSubscriptionType(SubscriptionType.ASSIGN);
        pullConsumerWrapper.setAssignedMessageQueue(assignedMessageQueue);
        RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper, prohibitionTopics);
        Mockito.verify(pullConsumer, Mockito.times(1)).assign(
                Mockito.any());
    }

    @Test
    public void testCacheAndRemovePullConsumer() {
        RocketMqPullConsumerController.cachePullConsumer(pullConsumer);
        Assert.assertEquals(RocketMqConsumerCache.PULL_CONSUMERS_CACHE.size(), 1);
        RocketMqPullConsumerController.removePullConsumer(pullConsumer);
        Assert.assertEquals(RocketMqConsumerCache.PULL_CONSUMERS_CACHE.size(), 0);
    }

    @AfterClass
    public static void tearDown() {
        Mockito.clearAllCaches();
    }

    private static ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClientIP("test-ip");
        clientConfig.setInstanceName("test-consumer");
        clientConfig.setNamesrvAddr("test-add");
        return clientConfig;
    }
}
