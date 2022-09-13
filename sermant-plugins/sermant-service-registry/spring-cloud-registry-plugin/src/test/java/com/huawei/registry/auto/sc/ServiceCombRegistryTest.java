/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.context.RegisterContext.ClientInfo;
import com.huawei.registry.entity.FixedResult;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServiceCombRegistry 注册类测试
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class ServiceCombRegistryTest {
    private final TestRegistryService service = new TestRegistryService();

    private final ServiceCombRegistry registry = new ServiceCombRegistry();

    private final TestRegistryService spyService = Mockito.spy(service);

    @Mock
    private ServiceCombRegistration registration;

    private MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(RegisterCenterService.class))
                .thenReturn(spyService);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
    }

    @After
    public void tearDown() {
        pluginServiceManagerMockedStatic.close();
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void register() {
        String host = "localhost";
        Map<String, String> meta = new HashMap<>();
        int port = 8888;
        String serviceId = "test";
        Mockito.when(registration.getHost()).thenReturn(host);
        Mockito.when(registration.getMetadata()).thenReturn(meta);
        Mockito.when(registration.getPort()).thenReturn(port);
        Mockito.when(registration.getServiceId()).thenReturn(serviceId);
        registry.register(registration);
        final ClientInfo clientInfo = RegisterContext.INSTANCE.getClientInfo();
        Assert.assertEquals(clientInfo.getHost(), host);
        Assert.assertEquals(clientInfo.getMeta(), meta);
        Assert.assertEquals(clientInfo.getPort(), port);
        Assert.assertEquals(clientInfo.getServiceId(), serviceId);
        Mockito.verify(spyService, Mockito.times(1)).register(Mockito.any());
        clientInfo.setServiceId(null);
        clientInfo.setHost(null);
        clientInfo.setZone(null);
        clientInfo.setMeta(null);
        clientInfo.setPort(0);
    }

    @Test
    public void deregister() {
        registry.deregister(Mockito.mock(ServiceCombRegistration.class));
        Mockito.verify(spyService, Mockito.times(1)).unRegister();
    }

    @Test
    public void status() {
        String status = "UP";
        registry.setStatus(registration, status);
        Assert.assertEquals(registry.getStatus(registration), status);
    }

    /**
     * 测试用
     *
     * @since 2022-09-06
     */
    public static class TestRegistryService implements RegisterCenterService {
        private String status;

        private List<MicroServiceInstance> instances;

        @Override
        public void register(FixedResult result) {

        }

        @Override
        public void unRegister() {

        }

        public void setInstances(List<MicroServiceInstance> instances) {
            this.instances = instances;
        }

        @Override
        public List<MicroServiceInstance> getServerList(String serviceId) {
            return instances;
        }

        @Override
        public List<String> getServices() {
            return null;
        }

        @Override
        public String getRegisterCenterStatus() {
            return null;
        }

        @Override
        public String getInstanceStatus() {
            return status;
        }

        @Override
        public void updateInstanceStatus(String status) {
            this.status = status;
        }
    }
}
