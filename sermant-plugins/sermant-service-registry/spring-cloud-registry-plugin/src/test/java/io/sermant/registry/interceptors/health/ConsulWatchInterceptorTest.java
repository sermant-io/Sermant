/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.registry.interceptors.health;

import io.sermant.registry.config.RegisterConfig;
import io.sermant.registry.context.RegisterContext;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Intercept to get watcher
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class ConsulWatchInterceptorTest {
    private final RegisterConfig registerConfig = new RegisterConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private ConsulWatchInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);
        interceptor = new ConsulWatchInterceptor();
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void doAfter() throws NoSuchMethodException {
        final ExecuteContext context = interceptor
                .doAfter(ExecuteContext.forMemberMethod(this, String.class.getDeclaredMethod("trim"), null, null,
                        null));
        Assert.assertEquals(context.getObject(), RegisterContext.INSTANCE.getRegisterWatch());
        RegisterContext.INSTANCE.setRegisterWatch(null);
    }
}
