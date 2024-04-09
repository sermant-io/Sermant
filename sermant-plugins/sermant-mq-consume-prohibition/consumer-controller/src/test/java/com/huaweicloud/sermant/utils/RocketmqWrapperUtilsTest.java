/*
 *  Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.utils;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultMqPushConsumerWrapper;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
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
import java.util.Optional;

/**
 * RocketmqWrapperUtil unit test
 *
 * @author daizhenyu
 * @since 2023-12-26
 **/
public class RocketmqWrapperUtilsTest {
    private static DefaultLitePullConsumer pullConsumer;

    private static DefaultMQPushConsumer pushConsumer;

    private static Collection<MessageQueue> messageQueues;

    private static MessageQueue messageQueue;

    @BeforeClass
    public static void setUp() throws MQClientException {
        MockedStatic<ConfigManager> configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());

        pullConsumer = new DefaultLitePullConsumer("test-group");
        pullConsumer.start();

        pushConsumer = Mockito.mock(DefaultMQPushConsumer.class);
        DefaultMQPushConsumerImpl pushConsumerImpl = Mockito.mock(DefaultMQPushConsumerImpl.class);
        MQClientInstance mqClientInstance = Mockito.mock(MQClientInstance.class);
        Mockito.when(pushConsumer.getDefaultMQPushConsumerImpl()).thenReturn(pushConsumerImpl);
        Mockito.when(pushConsumerImpl.getmQClientFactory()).thenReturn(mqClientInstance);
        Mockito.when(mqClientInstance.getClientConfig()).thenReturn(createClientConfig());

        messageQueues = new ArrayList<>();
        messageQueue = new MessageQueue("test-topic", "broker-1", 1);
        messageQueues.add(messageQueue);
    }

    @Test
    public void testWrapPullConsumer() {
        Optional<DefaultLitePullConsumerWrapper> pullConsumerWrapperOptional = RocketMqWrapperUtils
                .wrapPullConsumer(pullConsumer);
        Assert.assertTrue(pullConsumerWrapperOptional.isPresent());
        Assert.assertEquals(pullConsumerWrapperOptional.get().getPullConsumer(), pullConsumer);
    }

    @Test
    public void testWrapPushConsumer() {
        Optional<DefaultMqPushConsumerWrapper> pushConsumerWrapperOptional = RocketMqWrapperUtils
                .wrapPushConsumer(pushConsumer);
        Assert.assertTrue(pushConsumerWrapperOptional.isPresent());
        Assert.assertEquals(pushConsumerWrapperOptional.get().getPushConsumer(), pushConsumer);
    }

    private static ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClientIP("test-ip");
        clientConfig.setInstanceName("test-consumer");
        clientConfig.setNamesrvAddr("test-add");
        return clientConfig;
    }

    @AfterClass
    public static void tearDown() {
        Mockito.clearAllCaches();
    }
}
