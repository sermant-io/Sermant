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

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.RegisterConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Response测试
 *
 * @author zhouss
 * @since 2022-07-01
 */
public class ResponseTest {
    /**
     * 测试地址
     */
    public static final String SHUTDOWN_ENDPOINT = "localhost:8911";

    /**
     * PluginConfigManager mock对象
     */
    public MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    /**
     * 初始化
     */
    @Before
    public void init() {
        final GraceConfig graceConfig = new GraceConfig();
        graceConfig.setEnableSpring(true);
        graceConfig.setEnableGraceShutdown(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(graceConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }
}
