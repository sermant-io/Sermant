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
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.utils.CollectionUtils;

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
import java.util.Collection;
import java.util.Collections;
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
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(RegisterCenterService.class))
                .thenReturn(registerCenterService);
        final ZookeeperInstanceSupplierInterceptorTest test = new ZookeeperInstanceSupplierInterceptorTest();
        instanceList.add(test.buildInstance(8001));
        instanceList.add(test.buildInstance(8002));
        zkInstanceList.add(new ZookeeperServiceInstance(serviceName, test.buildZkInstance(8003)));
        zkInstanceList.add(new ZookeeperServiceInstance(serviceName, test.buildZkInstance(8004)));
        zkInstanceList.add(new ZookeeperServiceInstance(serviceName, test.buildZkInstance(8005)));
    }

    @Test
    public void doBefore() throws NoSuchMethodException {
        Mockito.when(registerCenterService.getServerList(serviceName)).thenReturn(instanceList);

        // isEmpty为false，isAvailable为true的普通场景
        RegisterContext.INSTANCE.setAvailable(true);
        REGISTER_CONFIG.setEnableSpringRegister(true);
        REGISTER_CONFIG.setOpenMigration(true);
        RegisterDynamicConfig.INSTANCE.setClose(false);
        final ExecuteContext context = interceptor.doBefore(
                buildContext(client, new Object[]{serviceName}, zkInstanceList));
        final ExecuteContext contextResult = interceptor.doAfter(context);
        Assert.assertTrue(contextResult.getResult() instanceof List);
        Assert.assertEquals(((List<?>) contextResult.getResult()).size(), zkInstanceList.size() + instanceList.size());

        // isEmpty为false，isAvailable为true的isWebfLux场景
        final ExecuteContext fluxContext = interceptor.doBefore(buildContext(reactiveDiscoveryClient,
                new Object[]{serviceName}, Flux.fromIterable(zkInstanceList)));
        final ExecuteContext fluxContextResult = interceptor.doAfter(fluxContext);
        Assert.assertTrue(fluxContextResult.getResult() instanceof Flux);
        final List<?> block = ((Flux<?>) fluxContextResult.getResult()).collectList().block();
        Assert.assertNotNull(block);
        Assert.assertEquals(block.size(), zkInstanceList.size() + instanceList.size());

        // isEmpty为false，isAvailable为false场景
        RegisterContext.INSTANCE.setAvailable(false);
        final ExecuteContext notAvailableContext = interceptor.doBefore(
                buildContext(client, new Object[]{serviceName}, zkInstanceList));
        Assert.assertTrue(notAvailableContext.isSkip());
        Assert.assertTrue(CollectionUtils.isEmpty((Collection<?>) notAvailableContext.getResult()));
        final ExecuteContext notAvailableContextResult = interceptor.doAfter(notAvailableContext);
        Assert.assertTrue(notAvailableContextResult.getResult() instanceof List);
        Assert.assertEquals(((List<?>) notAvailableContextResult.getResult()).size(), instanceList.size());

        // isEmpty为true，isAvailable为true场景
        RegisterContext.INSTANCE.setAvailable(true);
        Mockito.when(registerCenterService.getServerList(serviceName)).thenReturn(Collections.emptyList());
        final ExecuteContext contextWithEmptyList = interceptor.doBefore(
                buildContext(client, new Object[]{serviceName}, zkInstanceList));
        final ExecuteContext contextWithEmptyListResult = interceptor.doAfter(contextWithEmptyList);
        Assert.assertTrue(contextWithEmptyListResult.getResult() instanceof List);
        Assert.assertEquals(((List<?>) contextWithEmptyListResult.getResult()).size(), zkInstanceList.size());

        // isEmpty为true，isAvailable为false场景
        Mockito.when(registerCenterService.getServerList(serviceName)).thenReturn(Collections.emptyList());
        final ExecuteContext contextWithEmptyListx = interceptor.doBefore(
                buildContext(client, new Object[]{serviceName}, zkInstanceList));
        final ExecuteContext contextWithEmptyListResultx = interceptor.doAfter(contextWithEmptyListx);
        Assert.assertTrue(contextWithEmptyListResultx.getResult() instanceof List);
        Assert.assertEquals(((List<?>) contextWithEmptyListResultx.getResult()).size(), zkInstanceList.size());

        REGISTER_CONFIG.setEnableSpringRegister(false);
        REGISTER_CONFIG.setOpenMigration(false);
    }

    @Override
    protected DiscoveryClientInterceptor getInterceptor() {
        return new DiscoveryClientInterceptor();
    }
}
