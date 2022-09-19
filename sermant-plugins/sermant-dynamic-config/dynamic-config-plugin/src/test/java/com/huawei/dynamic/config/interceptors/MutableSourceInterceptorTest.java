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
import com.huawei.dynamic.config.closer.ConfigCenterCloserTest;
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
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Collections;

/**
 * MutableSource拦截器测试
 *
 * @author zhouss
 * @since 2022-09-05
 */
public class MutableSourceInterceptorTest {
    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    @After
    public void tearDown() throws Exception {
        operationManagerMockedStatic.close();
    }

    @Test
    public void test() {
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)) {
            final DynamicConfiguration configuration = new DynamicConfiguration();
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
                    .thenReturn(configuration);
            final MutableSourceInterceptor interceptor = new MutableSourceInterceptor();
            final ExecuteContext context = interceptor.doBefore(buildContext(null));
            Assert.assertFalse(context.isSkip());
            final OriginConfigDisableSource test = new OriginConfigDisableSource("test");
            ConfigHolder.INSTANCE.getConfigSources().add(test);
            final ExecuteContext zkContext = interceptor.doBefore(buildContext(false));
            Assert.assertTrue(zkContext.isSkip());
            final ExecuteContext nacosContext = interceptor.doBefore(buildContext(true));
            Assert.assertTrue(nacosContext.isSkip());
        } catch (Exception exception) {
            // ignored
        } finally {
            ConfigHolder.INSTANCE.getConfigSources()
                    .removeIf(configSource -> configSource.getClass() == OriginConfigDisableSource.class);
            Collections.sort(ConfigHolder.INSTANCE.getConfigSources());
        }
    }

    private ExecuteContext buildContext(Boolean isNacos) throws Exception {
        final BootstrapPropertySource bootstrapPropertySource = Mockito.mock(BootstrapPropertySource.class);
        final ConfigCenterCloserTest configCenterCloserTest = new ConfigCenterCloserTest();
        PropertySource source;
        if (isNacos == null) {
            source = new CompositePropertySource("test");
        } else if (isNacos) {
            source = configCenterCloserTest.buildNacosPropertySource();
        } else {
            source = configCenterCloserTest.buildZkSource();
        }
        Mockito.when(bootstrapPropertySource.getDelegate()).thenReturn(source);
        final ExecuteContext context = ExecuteContext.forMemberMethod(this, String.class.getDeclaredMethod("trim"),
                new Object[]{bootstrapPropertySource}, Collections.emptyMap(), Collections.emptyMap());
        context.changeResult(new Object());
        return context;
    }
}
