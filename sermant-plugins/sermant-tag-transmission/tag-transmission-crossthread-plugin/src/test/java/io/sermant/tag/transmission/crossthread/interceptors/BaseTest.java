/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.tag.transmission.crossthread.interceptors;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.CrossThreadConfig;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * test BaseClass
 *
 * @author provenceee
 * @since 2023-06-08
 */
public abstract class BaseTest {
    protected static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    @BeforeClass
    public static void before() {
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(CrossThreadConfig.class))
                .thenReturn(new CrossThreadConfig());
        TrafficUtils.removeTrafficTag();
    }

    /**
     * Release the mock object after UT execution
     */
    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
    }
}
