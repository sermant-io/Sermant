/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.auto.sc;

import com.huawei.registry.auto.sc.ServiceCombRegistryTest.TestRegistryService;
import com.huawei.registry.auto.sc.reactive.ServiceCombReactiveDiscoveryClientTest;
import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.netflix.client.config.IClientConfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

/**
 * ServiceCombService Test
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class ServiceCombServiceListTest {
    private final TestRegistryService service = new TestRegistryService();

    private final List<MicroServiceInstance> instances = Arrays.asList(
            ServiceCombReactiveDiscoveryClientTest.buildInstance(),
            ServiceCombReactiveDiscoveryClientTest.buildInstance());

    private final ServiceCombServiceList serviceCombServiceList = new ServiceCombServiceList();

    @Mock
    private IClientConfig clientConfig;

    private MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        service.setInstances(instances);
        MockitoAnnotations.openMocks(this);
        Mockito.when(clientConfig.getClientName()).thenReturn("test");
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(RegisterCenterService.class))
                .thenReturn(service);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
        initWithNiwsConfig();
    }

    @After
    public void tearDown() {
        pluginServiceManagerMockedStatic.close();
        pluginConfigManagerMockedStatic.close();
    }

    private void initWithNiwsConfig() {
        serviceCombServiceList.initWithNiwsConfig(clientConfig);
        Assert.assertTrue(ReflectUtils.getFieldValue(serviceCombServiceList, "clientConfig").isPresent());
    }

    @Test
    public void getInitialListOfServers() {
        final List<ServiceCombServer> initialListOfServers = serviceCombServiceList.getInitialListOfServers();
        Assert.assertEquals(initialListOfServers.size(), instances.size());
    }

    @Test
    public void getUpdatedListOfServers() {
        final List<ServiceCombServer> initialListOfServers = serviceCombServiceList.getUpdatedListOfServers();
        Assert.assertEquals(initialListOfServers.size(), instances.size());
    }
}
