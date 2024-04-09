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

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * KafkaConsumerPollInterceptor unit test
 *
 * @author lilai
 * @since 2023-12-23
 */
public class KafkaConsumerPollInterceptorTest {
    private KafkaConsumerPollInterceptor interceptor;

    private KafkaConsumer<?, ?> mockConsumer;

    private MockedStatic<ConfigManager> configManagerMockedStatic;

    private KafkaConsumerWrapper kafkaConsumerWrapper;

    @Before
    public void setUp() {
        interceptor = new KafkaConsumerPollInterceptor();

        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());

        mockConsumer = Mockito.mock(KafkaConsumer.class);
        KafkaConsumerController.addKafkaConsumerCache(mockConsumer);
        kafkaConsumerWrapper = KafkaConsumerController.getKafkaConsumerCache()
                .get(mockConsumer.hashCode());
        kafkaConsumerWrapper.getIsConfigChanged().set(true);
    }

    @After
    public void tearDown() {
        configManagerMockedStatic.close();
        KafkaConsumerController.removeKafkaConsumeCache(mockConsumer);
    }

    /**
     * Test before method
     */
    @Test
    public void testBefore() {
        ExecuteContext context = ExecuteContext.forMemberMethod(mockConsumer, null, null, null, null);
        interceptor.before(context);
        Assert.assertFalse(kafkaConsumerWrapper.getIsConfigChanged().get());
    }
}