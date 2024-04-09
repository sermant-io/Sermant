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

import static org.junit.Assert.*;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClient;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * Service discovery test service
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class DiscoveryClientServiceInterceptorTest extends BaseRegistryTest<DiscoveryClientServiceInterceptor> {
    private final List<String> services = new ArrayList<>();

    private final List<String> originServices = new ArrayList<>();

    @Mock
    private RegisterCenterService registerCenterService;

    @Mock
    private CompositeDiscoveryClient client;

    @Mock
    private ReactiveCompositeDiscoveryClient reactiveCompositeDiscoveryClient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(RegisterCenterService.class))
                .thenReturn(registerCenterService);
        services.add("test1");
        services.add("test2");
        Mockito.when(registerCenterService.getServices()).thenReturn(services);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Override
    protected DiscoveryClientServiceInterceptor getInterceptor() {
        return new DiscoveryClientServiceInterceptor();
    }

    @Test
    public void doBefore() throws NoSuchMethodException {
        // Normal scenarios where isAvailable is true
        RegisterContext.INSTANCE.setAvailable(true);
        REGISTER_CONFIG.setEnableSpringRegister(true);
        REGISTER_CONFIG.setOpenMigration(true);
        final ExecuteContext context = interceptor.doAfter(buildContext(client, null, originServices));
        Assert.assertTrue(context.getResult() instanceof List);
        Assert.assertEquals(((List<?>) context.getResult()).size(), originServices.size() + services.size());

        // IsWebfLux scenario where isAvailable is true
        final ExecuteContext fluxContext = interceptor.doAfter(
                buildContext(reactiveCompositeDiscoveryClient, null, Flux.fromIterable(originServices)));
        Assert.assertTrue(fluxContext.getResult() instanceof Flux);
        final List<?> block = ((Flux<?>) fluxContext.getResult()).collectList().block();
        Assert.assertNotNull(block);
        Assert.assertEquals(block.size(), originServices.size() + services.size());

        // A normal scenario where isAvailable is false
        RegisterContext.INSTANCE.setAvailable(false);
        final ExecuteContext NotAvailableContext = interceptor.doBefore(
                buildContext(client, null));
        Assert.assertTrue(NotAvailableContext.isSkip());
        REGISTER_CONFIG.setEnableSpringRegister(false);
        REGISTER_CONFIG.setOpenMigration(false);
    }

    @Test
    public void getInstanceClassName() {
    }
}
