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

package com.huawei.discovery.service.lb.discovery.zk;

import com.huawei.discovery.config.LbConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * 测试zk服务版本管理器
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class ZkServiceManagerTest {
    private final LbConfig lbConfig = new LbConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(lbConfig);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    /**
     * 无服务抛出异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void chooseService() {
        final ZkServiceManager zkServiceManager = new ZkServiceManager();
        zkServiceManager.chooseService();
    }

    /**
     * 无该版本抛出异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void chooseServiceWithNoVersion() {
        lbConfig.setZkServerVersion("9.9.9");
        final ZkServiceManager zkServiceManager = new ZkServiceManager();
        zkServiceManager.chooseService();
    }

    /**
     * 存在指定版本
     */
    @Test
    public void chooseServiceWithVersion() {
        lbConfig.setZkServerVersion("3.4.14");
        final ZkService34 service34 = Mockito.mock(ZkService34.class);
        try (final MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic =
                Mockito.mockStatic(PluginServiceManager.class)){
            pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(ZkService34.class))
            .thenReturn(service34);
            final ZkServiceManager zkServiceManager = new ZkServiceManager();
            Assert.assertEquals(zkServiceManager.chooseService(), service34);
        }
    }
}
