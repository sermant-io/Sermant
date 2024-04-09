/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.discovery.entity;

import com.huawei.discovery.config.DiscoveryPluginConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple logger test
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class SimpleRequestRecorderTest {
    private final DiscoveryPluginConfig discoveryPluginConfig = new DiscoveryPluginConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        discoveryPluginConfig.setEnableRequestCount(true);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class))
                .thenReturn(discoveryPluginConfig);
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void beforeRequest() {
        final SimpleRequestRecorder simpleRequestRecorder = new SimpleRequestRecorder();
        simpleRequestRecorder.beforeRequest();
        final Optional<Object> allRequestCount = ReflectUtils.getFieldValue(simpleRequestRecorder, "allRequestCount");
        Assert.assertTrue(allRequestCount.isPresent() && allRequestCount.get() instanceof AtomicLong);
        Assert.assertTrue(((AtomicLong) allRequestCount.get()).get() > 0);

        // Set the maximum so that the overflow is set to 0
        ((AtomicLong) allRequestCount.get()).set(Long.MAX_VALUE);
        simpleRequestRecorder.beforeRequest();
        Assert.assertEquals(((AtomicLong) allRequestCount.get()).get(), 0);

        // Print the logs
        discoveryPluginConfig.setEnableRequestCount(true);
        final SimpleRequestRecorder simpleRequestRecorder1 = new SimpleRequestRecorder();
        simpleRequestRecorder1.beforeRequest();
    }
}
