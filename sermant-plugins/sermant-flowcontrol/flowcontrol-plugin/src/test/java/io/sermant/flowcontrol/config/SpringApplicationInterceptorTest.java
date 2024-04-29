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

package io.sermant.flowcontrol.config;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.init.FlowControlInitServiceImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * start subscription test
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class SpringApplicationInterceptorTest {
    private final FlowControlConfig flowControlConfig = new FlowControlConfig();

    private final AtomicBoolean executed = new AtomicBoolean();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
    }

    @Before
    public void setUp() {
        flowControlConfig.setUseCseRule(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(flowControlConfig);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(FlowControlInitServiceImpl.class))
                .thenReturn(new FlowControlInitServiceImpl(){
                    @Override
                    public void doStart() {
                        executed.set(true);
                    }
                });
    }

    /**
     * test start
     */
    @Test
    public void testStart() throws Exception {
        final SpringApplicationInterceptor springApplicationInterceptor = new SpringApplicationInterceptor();
        springApplicationInterceptor.after(buildContext());
        Assert.assertTrue(executed.get());
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(
            new TestObject(),
            String.class.getMethod("trim"),
            new Object[0],
            Collections.emptyMap(),
            Collections.emptyMap()
        );
    }

    private static class TestObject {
        private final boolean logStartupInfo = true;
    }
}