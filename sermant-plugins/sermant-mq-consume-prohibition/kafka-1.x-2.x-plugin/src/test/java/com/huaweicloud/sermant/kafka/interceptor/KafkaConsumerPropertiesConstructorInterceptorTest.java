/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huaweicloud.sermant.core.utils.NetworkUtils;
import com.huaweicloud.sermant.kafka.cache.KafkaConsumerWrapper;
import com.huaweicloud.sermant.kafka.controller.KafkaConsumerController;
import com.huaweicloud.sermant.kafka.utils.MarkUtils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashSet;

/**
 * KafkaConsumerPropertiesConstructorInterceptor Unit Test
 *
 * @author lilai
 * @since 2023-12-23
 */
public class KafkaConsumerPropertiesConstructorInterceptorTest {
    private KafkaConsumerPropertiesConstructorInterceptor interceptor;

    private MockedStatic<MarkUtils> markUtilsMockedStatic;

    private MockedStatic<ConfigManager> configManagerMockedStatic;

    private KafkaConsumer<?, ?> mockConsumer;

    @Before
    public void setUp() {
        interceptor = new KafkaConsumerPropertiesConstructorInterceptor();
        mockConsumer = Mockito.mock(KafkaConsumer.class);
        markUtilsMockedStatic = Mockito.mockStatic(MarkUtils.class);
        markUtilsMockedStatic.when(MarkUtils::getMark).thenReturn(null);
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());
    }

    @After
    public void tearDown() {
        markUtilsMockedStatic.close();
        configManagerMockedStatic.close();
        KafkaConsumerController.removeKafkaConsumeCache(mockConsumer);
    }

    /**
     * Test after method
     */
    @Test
    public void testAfter() {
        mockConsumer = Mockito.mock(KafkaConsumer.class);
        ExecuteContext context = ExecuteContext.forMemberMethod(mockConsumer, null, null, null, null);

        interceptor.after(context);

        KafkaConsumerWrapper wrapper = KafkaConsumerController.getKafkaConsumerCache()
                .get(mockConsumer.hashCode());
        Assert.assertNotNull(wrapper);
        Assert.assertEquals(mockConsumer, wrapper.getKafkaConsumer());
        Assert.assertEquals("default", wrapper.getApplication());
        Assert.assertEquals("default", wrapper.getService());
        Assert.assertEquals("default", wrapper.getZone());
        Assert.assertEquals("default", wrapper.getProject());
        Assert.assertEquals("", wrapper.getEnvironment());
        Assert.assertFalse(wrapper.isAssign());
        Assert.assertFalse(wrapper.getIsConfigChanged().get());
        Assert.assertEquals(new HashSet<>(), wrapper.getOriginalTopics());
        Assert.assertEquals(new HashSet<>(), wrapper.getOriginalPartitions());
        Assert.assertEquals(NetworkUtils.getMachineIp(), wrapper.getServerAddress());
    }
}