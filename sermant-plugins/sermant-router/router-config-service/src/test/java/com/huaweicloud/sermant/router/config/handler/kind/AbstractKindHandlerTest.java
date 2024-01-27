/*
 *
 *  * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.huaweicloud.sermant.router.config.handler.kind;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.config.EventConfig;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * 测试抽象类
 *
 * @author provenceee
 * @since 2024-01-16
 */
public abstract class AbstractKindHandlerTest {
    private static MockedStatic<ConfigManager> mockConfigManager;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    /**
     * 初始化
     */
    @BeforeClass
    public static void init() {
        EventConfig config = new EventConfig();
        mockConfigManager = Mockito.mockStatic(ConfigManager.class);
        mockConfigManager.when(() -> ConfigManager.getConfig(EventConfig.class)).thenReturn(config);

        RouterConfig routerConfig = new RouterConfig();
        routerConfig.setZone("foo");
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RouterConfig.class))
                .thenReturn(routerConfig);
    }

    /**
     * 清除mock
     */
    @AfterClass
    public static void clear() {
        mockConfigManager.close();
        mockPluginConfigManager.close();
    }
}
