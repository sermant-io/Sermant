/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.kafka.interceptor;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.kafka.cache.KafkaConsumerWrapper;
import com.huaweicloud.sermant.kafka.controller.KafkaConsumerController;
import com.huaweicloud.sermant.utils.InvokeUtils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashSet;

/**
 * KafkaConsumerUnsubscribeInterceptor Unit Test
 *
 * @author lilai
 * @since 2023-12-23
 */
public class KafkaConsumerUnSubscribeInterceptorTest {
    private KafkaConsumerUnSubscribeInterceptor interceptor;

    private KafkaConsumer<?, ?> mockConsumer;

    private MockedStatic<InvokeUtils> invokeUtilsMockedStatic;

    private MockedStatic<ConfigManager> configManagerMockedStatic;

    private HashSet<String> topics;

    private HashSet<TopicPartition> topicPartitions;

    private KafkaConsumerWrapper kafkaConsumerWrapper;

    @Before
    public void setUp() {
        interceptor = new KafkaConsumerUnSubscribeInterceptor();
        invokeUtilsMockedStatic = Mockito.mockStatic(InvokeUtils.class);
        invokeUtilsMockedStatic.when(() -> InvokeUtils.isKafkaInvokeBySermant(Thread.currentThread().getStackTrace()))
                .thenReturn(false);
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());

        mockConsumer = Mockito.mock(KafkaConsumer.class);
        KafkaConsumerController.addKafkaConsumerCache(mockConsumer);
        kafkaConsumerWrapper = KafkaConsumerController.getKafkaConsumerCache()
                .get(mockConsumer.hashCode());
        topics = new HashSet<>();
        topics.add("testTopic-1");
        topicPartitions = new HashSet<>();
        topicPartitions.add(new TopicPartition("testTopic-1", 0));
        kafkaConsumerWrapper.setOriginalTopics(topics);
        kafkaConsumerWrapper.setOriginalPartitions(topicPartitions);
        kafkaConsumerWrapper.setAssign(true);
    }

    @After
    public void tearDown() {
        invokeUtilsMockedStatic.close();
        configManagerMockedStatic.close();
        KafkaConsumerController.removeKafkaConsumeCache(mockConsumer);
    }

    /**
     * Test after method
     */
    @Test
    public void testAfter() {
        ExecuteContext context = ExecuteContext.forMemberMethod(mockConsumer, null, null, null, null);
        interceptor.after(context);
        Assert.assertEquals(0, kafkaConsumerWrapper.getOriginalPartitions().size());
        Assert.assertEquals(0, kafkaConsumerWrapper.getOriginalTopics().size());
        Assert.assertFalse(kafkaConsumerWrapper.isAssign());
    }
}