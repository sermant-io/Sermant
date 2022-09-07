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

package com.huawei.registry.interceptors.health;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.context.RegisterContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * eureka健康测试
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class EurekaHealthInterceptorTest {
    private final RegisterConfig registerConfig = new RegisterConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private EurekaHealthInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);
        interceptor = new EurekaHealthInterceptor();
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void doBefore() throws NoSuchMethodException {
        final ExecuteContext context = interceptor.doBefore(buildContext());
        Assert.assertFalse(context.isSkip());
        registerConfig.setOpenMigration(true);
        registerConfig.setEnableSpringRegister(true);
        RegisterDynamicConfig.INSTANCE.setClose(true);
        final ExecuteContext context1 = interceptor.doBefore(buildContext());
        Assert.assertTrue(context1.isSkip());
        RegisterDynamicConfig.INSTANCE.setClose(false);
    }

    @Test
    public void doAfter() throws NoSuchMethodException {
        final ExecuteContext context = buildContext();
        context.changeResult(true);
        interceptor.doAfter(context);
        Assert.assertTrue(RegisterContext.INSTANCE.isAvailable());
        context.changeResult(false);
        interceptor.doAfter(context);
        Assert.assertFalse(RegisterContext.INSTANCE.isAvailable());
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(this, String.class.getDeclaredMethod("trim"),
                null, null, null);
    }
}
