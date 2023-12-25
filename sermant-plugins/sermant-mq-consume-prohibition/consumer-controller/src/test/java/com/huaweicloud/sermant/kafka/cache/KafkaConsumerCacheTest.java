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

package com.huaweicloud.sermant.kafka.cache;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.utils.NetworkUtils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * KafkaConsumerCache单元测试
 *
 * @author lilai
 * @since 2023-12-23
 */
public class KafkaConsumerCacheTest {
    private MockedStatic<ConfigManager> configManagerMockedStatic;

    @Before
    public void setUp() {
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());
    }

    @After
    public void tearDown() {
        configManagerMockedStatic.close();
    }

    /**
     * 测试addKafkaConsumer方法
     */
    @Test
    public void testAddKafkaConsumer() {
        KafkaConsumer<?, ?> mockConsumer = Mockito.mock(KafkaConsumer.class);
        int hashCode = mockConsumer.hashCode();
        KafkaConsumerCache.INSTANCE.addKafkaConsumer(mockConsumer);

        Assert.assertTrue(KafkaConsumerCache.INSTANCE.getCache().containsKey(hashCode));
        Assert.assertEquals(mockConsumer, KafkaConsumerCache.INSTANCE.getCache().get(hashCode).getKafkaConsumer());
    }

    /**
     * 测试convert方法
     */
    @Test
    public void testConvert() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        KafkaConsumer<?, ?> mockConsumer = Mockito.mock(KafkaConsumer.class);
        Method method = KafkaConsumerCache.class.getDeclaredMethod("convert", KafkaConsumer.class);
        method.setAccessible(true);
        KafkaConsumerWrapper wrapper = (KafkaConsumerWrapper) method.invoke(KafkaConsumerCache.INSTANCE, mockConsumer);

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