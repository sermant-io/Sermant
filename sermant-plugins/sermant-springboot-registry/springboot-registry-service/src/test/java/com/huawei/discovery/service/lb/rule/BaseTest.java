/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.service.lb.rule;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.service.lb.utils.CommonUtils;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * 基础测试
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class BaseTest {
    protected final LbConfig lbConfig = new LbConfig();

    protected MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    protected MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(lbConfig);
        pluginServiceManagerMockedStatic = Mockito
                .mockStatic(PluginServiceManager.class);
    }

    @After
    public void tearDown() {
        CommonUtils.cleanServiceStats();
        pluginConfigManagerMockedStatic.close();
        pluginServiceManagerMockedStatic.close();
    }
}
