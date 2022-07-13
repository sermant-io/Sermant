/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config;

import com.huawei.dynamic.config.sources.TestConfigSources;
import com.huawei.dynamic.config.sources.TestLowestConfigSources;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 配置配置
 *
 * @author zhouss
 * @since 2022-04-16
 */
public class ConfigHolderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String KEY = "test";
    private static final String CONTENT = "test: 1";

    private static final int TEST_CONFIG_SOURCES_SIZE = 3;

    private static DynamicConfigEvent event;

    @BeforeClass
    public static void before() {
        event = Mockito.mock(DynamicConfigEvent.class);
        DynamicConfiguration configuration = Mockito.mock(DynamicConfiguration.class);
        Mockito.when(event.getKey()).thenReturn(KEY);
        Mockito.when(event.getContent()).thenReturn(CONTENT);
        Mockito.when(configuration.getFirstRefreshDelayMs()).thenReturn(0L);
        Mockito.mockStatic(PluginConfigManager.class)
            .when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
            .thenReturn(configuration);
    }

    @Test
    public void testResolveAndListener() {
        ConfigHolder.INSTANCE.addListener(event -> LOGGER.info("refresh success"));
        ConfigHolder.INSTANCE.resolve(event);
        final int test = (Integer) ConfigHolder.INSTANCE.getConfig(KEY);
        Assert.assertEquals(TestConfigSources.ORDER, test);
        final Set<String> configNames = ConfigHolder.INSTANCE.getConfigNames();
        Assert.assertTrue(configNames.contains(KEY));
    }

    @Test
    public void testConfigSourcesPriority() {
        final List<ConfigSource> configSources = ConfigHolder.INSTANCE.getConfigSources();
        if (configSources.size() == TEST_CONFIG_SOURCES_SIZE) {
            Assert.assertEquals(configSources.get(0).order(), TestConfigSources.ORDER);
            Assert.assertEquals(configSources.get(configSources.size() - 1).order(), TestLowestConfigSources.ORDER);
        }
    }
}
