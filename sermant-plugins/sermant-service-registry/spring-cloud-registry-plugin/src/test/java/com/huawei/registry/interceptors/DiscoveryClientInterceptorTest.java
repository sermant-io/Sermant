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

package com.huawei.registry.interceptors;

import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.interceptors.cloud3.x.ZookeeperInstanceSupplierInterceptorTest;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceInstance;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试服务发现
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class DiscoveryClientInterceptorTest extends BaseRegistryTest<DiscoveryClientInterceptor> {
    private final List<MicroServiceInstance> instanceList = new ArrayList<>();
    private final List<ServiceInstance> zkInstanceList = new ArrayList<>();

    private final String serviceName = "test";

    @Mock
    private RegisterCenterService registerCenterService;

    @Mock
    private CompositeDiscoveryClient client;

    @Mock
    private ReactiveCompositeDiscoveryClient reactiveDiscoveryClient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(RegisterCenterService.class))
                .thenReturn(registerCenterService);
        final ZookeeperInstanceSupplierInterceptorTest test = new ZookeeperInstanceSupplierInterceptorTest();
        instanceList.add(test.buildInstance(8001));
        instanceList.add(test.buildInstance(8002));
        zkInstanceList.add(new ZookeeperServiceInstance(serviceName, test.buildZkInstance(8003)));
        zkInstanceList.add(new ZookeeperServiceInstance(serviceName, test.buildZkInstance(8004)));
        Mockito.when(registerCenterService.getServerList(serviceName)).thenReturn(instanceList);
        Mockito.when(client.getInstances(serviceName)).thenReturn(zkInstanceList);
        Mockito.when(reactiveDiscoveryClient.getInstances(serviceName)).thenReturn(Flux.fromIterable(zkInstanceList));
    }

    @Test
    public void doBefore() throws NoSuchMethodException {
        RegisterContext.INSTANCE.setAvailable(true);
        REGISTER_CONFIG.setEnableSpringRegister(true);
        REGISTER_CONFIG.setOpenMigration(true);
        RegisterDynamicConfig.INSTANCE.setClose(false);
        final ExecuteContext context = interceptor.doBefore(buildContext(client, new Object[]{serviceName}));
        Assert.assertTrue(context.isSkip());
        Assert.assertTrue(context.getResult() instanceof List);
        Assert.assertEquals(((List<?>) context.getResult()).size(), zkInstanceList.size() + instanceList.size());

        final ExecuteContext fluxContext = interceptor.doBefore(buildContext(reactiveDiscoveryClient, new Object[]{serviceName}));
        Assert.assertTrue(fluxContext.isSkip());
        Assert.assertTrue(fluxContext.getResult() instanceof Flux);
        final List<?> block = ((Flux<?>) fluxContext.getResult()).collectList().block();
        Assert.assertNotNull(block);
        Assert.assertEquals(block.size(), zkInstanceList.size() + instanceList.size());
        RegisterContext.INSTANCE.setAvailable(false);
        REGISTER_CONFIG.setEnableSpringRegister(false);
        REGISTER_CONFIG.setOpenMigration(false);
    }

    @Override
    protected DiscoveryClientInterceptor getInterceptor() {
        return new DiscoveryClientInterceptor();
    }
}
