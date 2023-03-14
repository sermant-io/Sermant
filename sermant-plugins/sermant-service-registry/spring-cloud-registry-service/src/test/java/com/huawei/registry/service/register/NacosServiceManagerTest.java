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

import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.config.RegisterServiceCommonConfig;
import com.huawei.registry.context.RegisterContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * 测试管理器
 *
 * @author chengyouling
 * @since 2022-11-10
 */
public class NacosServiceManagerTest {
    private final NacosRegisterConfig registerConfig = new NacosRegisterConfig();
    private final RegisterServiceCommonConfig commonConfig = new RegisterServiceCommonConfig();
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;
    private NacosServiceManager nacosServiceManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        commonConfig.setAddress("127.0.0.1:8848");
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager
                .getPluginConfig(RegisterServiceCommonConfig.class)).thenReturn(commonConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager
                .getPluginConfig(NacosRegisterConfig.class)).thenReturn(registerConfig);
        RegisterContext.INSTANCE.getClientInfo().setServiceName("test");
        RegisterContext.INSTANCE.getClientInfo().setIp("127.0.0.1");
        RegisterContext.INSTANCE.getClientInfo().setServiceId("test");
        RegisterContext.INSTANCE.getClientInfo().setHost("localhost");
        RegisterContext.INSTANCE.getClientInfo().setPort(8001);
        RegisterContext.INSTANCE.getClientInfo().setMeta(new HashMap<>());
        nacosServiceManager = new NacosServiceManager();
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void testBuildNacosInstanceFromRegistration() {
        Instance instance = nacosServiceManager.buildNacosInstanceFromRegistration();
        Assert.assertNotNull(instance);
    }
}
