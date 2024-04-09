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

package com.huawei.registry.interceptors;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.context.RegisterContext.ClientInfo;
import com.huawei.registry.entity.FixedResult;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Register for the test
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class RegistrationInterceptorTest extends BaseRegistryTest<RegistrationInterceptor> {
    @Mock
    private Registration registration;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(RegisterCenterService.class))
                .thenReturn(new RegisterCenterService() {
                    @Override
                    public void register(FixedResult result) {
                        result.setSkip(true);
                    }

                    @Override
                    public void unRegister() {

                    }

                    @Override
                    public List<MicroServiceInstance> getServerList(String serviceId) {
                        return null;
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
                        return null;
                    }

                    @Override
                    public void updateInstanceStatus(String status) {

                    }
                });
    }

    @Test
    public void doBefore() throws NoSuchMethodException {
        REGISTER_CONFIG.setOpenMigration(true);
        REGISTER_CONFIG.setEnableSpringRegister(true);
        String host = "localhost";
        String serviceName = "test";
        int port = 8080;
        Map<String, String> meta = new HashMap<>();
        Mockito.when(registration.getHost()).thenReturn(host);
        Mockito.when(registration.getServiceId()).thenReturn(serviceName);
        Mockito.when(registration.getPort()).thenReturn(port);
        Mockito.when(registration.getMetadata()).thenReturn(meta);
        final ExecuteContext context = interceptor.doBefore(buildContext(this, new Object[] {registration}));
        Assert.assertTrue(context.isSkip());
        final ClientInfo clientInfo = RegisterContext.INSTANCE.getClientInfo();
        Assert.assertEquals(clientInfo.getHost(), host);
        Assert.assertEquals(clientInfo.getServiceId(), serviceName);
        Assert.assertEquals(clientInfo.getPort(), port);
        Assert.assertEquals(clientInfo.getMeta(), meta);
        clientInfo.setPort(0);
        clientInfo.setHost(null);
        clientInfo.setServiceId(null);
        clientInfo.setMeta(null);
        REGISTER_CONFIG.setOpenMigration(false);
        REGISTER_CONFIG.setEnableSpringRegister(false);

    }

    @Override
    protected RegistrationInterceptor getInterceptor() {
        return new RegistrationInterceptor();
    }
}
