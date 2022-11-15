/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.service.register;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.huawei.registry.config.NacosRegisterConfig;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * 测试管理器
 *
 * @author chengyouling
 * @since 2022-11-10
 */
public class NacosServiceManagerTest {
    private final NacosRegisterConfig registerConfig = new NacosRegisterConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    final NacosServiceManager nacosServiceManager = Mockito.mock(NacosServiceManager.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        registerConfig.setAddress("127.0.0.1:8848");
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(NacosRegisterConfig.class))
                .thenReturn(registerConfig);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void testGetNamingService() {
        final NacosNamingService namingService = Mockito.mock(NacosNamingService.class);
        Mockito.when(nacosServiceManager.getNamingService()).thenReturn(namingService);
        Assert.assertEquals(nacosServiceManager.getNamingService(), namingService);
    }

    @Test
    public void testGetNamingMaintainService(){
        final NacosNamingMaintainService namingMaintainService = Mockito.mock(NacosNamingMaintainService.class);
        Mockito.when(nacosServiceManager.getNamingMaintainService()).thenReturn(namingMaintainService);
        Assert.assertEquals(nacosServiceManager.getNamingMaintainService(), namingMaintainService);
    }
}
