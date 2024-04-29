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

package io.sermant.dynamic.config.source;

import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.subscribe.processor.OrderConfigEvent;
import io.sermant.dynamic.config.ConfigHolder;
import io.sermant.dynamic.config.DynamicConfiguration;
import io.sermant.dynamic.config.sources.MockEnvironment;
import io.sermant.dynamic.config.sources.TestConfigSources;
import io.sermant.dynamic.config.sources.TestLowestConfigSources;
import io.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.PropertySource;

import java.util.Collections;

/**
 * configuration source test
 *
 * @author zhouss
 * @since 2022-04-16
 */
public class SpringEnvironmentProcessorTest {
    private static final String KEY = "test";
    private static final String VALUE = String.valueOf(Integer.MIN_VALUE);
    private static final String CONTENT = "test: " + VALUE;
    private OrderConfigEvent event;
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        event = Mockito.mock(OrderConfigEvent.class);
        Mockito.when(event.getKey()).thenReturn(KEY);
        Mockito.when(event.getContent()).thenReturn(CONTENT);
        Mockito.when(event.getAllData()).thenReturn(Collections.singletonMap(KEY, VALUE));
        final DynamicConfiguration configuration = Mockito.mock(DynamicConfiguration.class);
        Mockito.when(configuration.getFirstRefreshDelayMs()).thenReturn(0L);
        Mockito.when(configuration.isEnableCseAdapter()).thenReturn(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
            .thenReturn(configuration);
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
            .thenReturn(new YamlConverterImpl());
        ConfigHolder.INSTANCE.getConfigSources().removeIf(configSource -> configSource.getClass() == TestConfigSources.class
                || configSource.getClass() == TestLowestConfigSources.class);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        operationManagerMockedStatic.close();
    }

    @Test
    public void locate() throws InterruptedException {
        final SpringEnvironmentProcessor springEnvironmentProcessor = new SpringEnvironmentProcessor();
        final MockEnvironment mockEnvironment = new MockEnvironment();
        springEnvironmentProcessor.postProcessEnvironment(mockEnvironment, null);
        final PropertySource<?> source = mockEnvironment.getPropertySources().get("Sermant-Dynamic-Config");
        Assert.assertNotNull(source);
        // Note the configSource injection test is performed. The spi file is viewed and sorted in the specified order
        ConfigHolder.INSTANCE.resolve(event);
        // Because this is an asynchronous execution, it waits for the asynchronous execution to complete
        Thread.sleep(1000);
        Assert.assertEquals(mockEnvironment.getProperty(KEY), VALUE);
    }
}
