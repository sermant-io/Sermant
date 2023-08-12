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

package com.huaweicloud.sermant.router.spring;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.TransmitConfig;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * mock TransmitConfig测试基类
 *
 * @author provenceee
 * @since 2023-06-08
 */
public abstract class BaseTransmitConfigTest {
    protected static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void initTransmitConfig() {
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(TransmitConfig.class))
                .thenReturn(new TransmitConfig());
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void closeMock() {
        mockPluginConfigManager.close();
    }
}