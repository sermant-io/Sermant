/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.interceptors;

import com.huawei.dynamic.config.ConfigHolder;
import com.huawei.dynamic.config.DynamicConfiguration;
import com.huawei.dynamic.config.source.OriginConfigDisableSource;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * test zk intercept
 *
 * @author zhouss
 * @since 2022-09-05
 */
public class ZookeeperLocatorInterceptorTest {
    private MockedStatic<OperationManager> operationManagerMockedStatic;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
            .thenReturn(new YamlConverterImpl());

        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        final DynamicConfiguration configuration = new DynamicConfiguration();
        configuration.setEnableOriginConfigCenter(false);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
            .thenReturn(configuration);

        ConfigHolder.INSTANCE.getConfigSources()
            .removeIf(configSource -> configSource.getClass() == OriginConfigDisableSource.class);
    }

    @After
    public void tearDown() {
        operationManagerMockedStatic.close();
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void test() {
        try {
            final ZookeeperLocatorInterceptor zookeeperLocatorInterceptor = new ZookeeperLocatorInterceptor();
            final ExecuteContext context = zookeeperLocatorInterceptor.doBefore(buildContext());
            Assert.assertTrue(context.isSkip());
            ConfigHolder.INSTANCE.getConfigSources().add(new OriginConfigDisableSource("test"));
            final ExecuteContext closeContext = zookeeperLocatorInterceptor.doBefore(buildContext());
            Assert.assertTrue(closeContext.isSkip());
        } catch (NoSuchMethodException e) {
            // ignored
        } finally {
            ConfigHolder.INSTANCE.getConfigSources()
                .removeIf(configSource -> configSource.getClass() == OriginConfigDisableSource.class);
            Collections.sort(ConfigHolder.INSTANCE.getConfigSources());
        }
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(new Object(), String.class.getDeclaredMethod("trim"), null, null, null);
    }
}
