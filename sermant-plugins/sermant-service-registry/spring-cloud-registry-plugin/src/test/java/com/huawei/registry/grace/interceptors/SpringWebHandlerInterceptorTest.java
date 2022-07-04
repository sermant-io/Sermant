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

package com.huawei.registry.grace.interceptors;

import static org.junit.Assert.*;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.services.GraceService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.servlet.HandlerExecutionChain;

import java.util.Objects;

/**
 * 添加拦截器测试
 *
 * @author zhouss
 * @since 2022-06-30
 */
public class SpringWebHandlerInterceptorTest {
    private final GraceConfig graceConfig = new GraceConfig();

    /**
     * 初始化
     */
    @Before
    public void init() {
        graceConfig.setEnableSpring(true);
        final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(graceConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
        Mockito.mockStatic(PluginServiceManager.class)
                .when(() -> PluginServiceManager.getPluginService(GraceService.class))
                .thenReturn(new GraceService() {
                    @Override
                    public void shutdown() {

                    }

                    @Override
                    public void addAddress(String address) {

                    }
                });
    }

    /**
     * 测试添加拦截器
     */
    @Test
    public void testAddInterceptor() {
        final HandlerExecutionChain chain = new HandlerExecutionChain(new Object());
        final ExecuteContext executeContext = ExecuteContext.forMemberMethod(chain, null, null, null, null);
        final SpringWebHandlerInterceptor springWebHandlerInterceptor = new SpringWebHandlerInterceptor();
        springWebHandlerInterceptor.doBefore(executeContext);
        Assert.assertTrue(Objects.requireNonNull(chain.getInterceptors()).length > 0);
    }
}
