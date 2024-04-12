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

package com.huaweicloud.sermant.rocketmq.controller;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.rocketmq.cache.RocketMqConsumerCache;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultMqPushConsumerWrapper;
import com.huaweicloud.sermant.utils.RocketMqWrapperUtils;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * RocketMqPushConsumerController unit test
 *
 * @author daizhenyu
 * @since 2023-12-27
 **/
public class RocketMqPushConsumerControllerTest {
    private static DefaultMQPushConsumer pushConsumer;

    private static DefaultMqPushConsumerWrapper pushConsumerWrapper;

    private static Set<String> prohibitionTopics;

    private static Set<String> subscribedTopics;

    @BeforeClass
    public static void setUp() throws MQClientException {
        MockedStatic<ConfigManager> configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());

        pushConsumer = Mockito.mock(DefaultMQPushConsumer.class);
        DefaultMQPushConsumerImpl pushConsumerImpl = Mockito.mock(DefaultMQPushConsumerImpl.class);
        MQClientInstance instance = Mockito.mock(MQClientInstance.class);
        Mockito.when(instance.getClientConfig()).thenReturn(createClientConfig());

        pushConsumerWrapper = new DefaultMqPushConsumerWrapper(pushConsumer, pushConsumerImpl, instance);
        pushConsumerWrapper.setConsumerGroup("test-group");

        MockedStatic<RocketMqWrapperUtils> wrapperUtilsMockedStatic = Mockito
                .mockStatic(RocketMqWrapperUtils.class);
        wrapperUtilsMockedStatic.when(() -> RocketMqWrapperUtils.wrapPushConsumer(pushConsumer))
                .thenReturn(Optional.of(pushConsumerWrapper));

        prohibitionTopics = new HashSet<>();
        prohibitionTopics.add("test-topic-1");
    }

    /**
     * Consumer subscription topic is empty
     */
    @Test
    public void testDisablePushConsumptionNoTopic() {
        pushConsumerWrapper.setProhibition(true);
        RocketMqPushConsumerController.disablePushConsumption(pushConsumerWrapper, prohibitionTopics);
        Assert.assertFalse(pushConsumerWrapper.isProhibition());
    }

    /**
     * Consumer subscription topics intersect with prohibited consumption topics
     */
    @Test
    public void testDisablePullConsumptionWithSubTractTopics() {
        subscribedTopics = new HashSet<>();
        subscribedTopics.add("test-topic-1");
        subscribedTopics.add("test-topic-2");
        pushConsumerWrapper.setSubscribedTopics(subscribedTopics);
        pushConsumerWrapper.setProhibition(false);
        RocketMqPushConsumerController.disablePushConsumption(pushConsumerWrapper, prohibitionTopics);
        Assert.assertTrue(pushConsumerWrapper.isProhibition());

        // 禁消费后，再次下发禁消费
        MQClientInstance clientFactory = pushConsumerWrapper.getClientFactory();
        Mockito.reset(clientFactory);
        RocketMqPushConsumerController.disablePushConsumption(pushConsumerWrapper, prohibitionTopics);
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
        pushConsumerWrapper.setSubscribedTopics(subscribedTopics);
        pushConsumerWrapper.setProhibition(true);
        RocketMqPushConsumerController.disablePushConsumption(pushConsumerWrapper, prohibitionTopics);
        Assert.assertFalse(pushConsumerWrapper.isProhibition());

        // 恢复消费后，再次下发禁消费配置
        MQClientInstance clientFactory = pushConsumerWrapper.getClientFactory();
        Mockito.reset(clientFactory);
        RocketMqPushConsumerController.disablePushConsumption(pushConsumerWrapper, prohibitionTopics);
        Mockito.verify(clientFactory, Mockito.times(0))
                .registerConsumer(Mockito.any(), Mockito.any());
    }

    @Test
    public void testCacheAndRemovePushConsumer() {
        RocketMqPushConsumerController.cachePushConsumer(pushConsumer);
        Assert.assertEquals(RocketMqConsumerCache.PUSH_CONSUMERS_CACHE.size(), 1);
        RocketMqPushConsumerController.removePushConsumer(pushConsumer);
        Assert.assertEquals(RocketMqConsumerCache.PUSH_CONSUMERS_CACHE.size(), 0);
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
