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

package io.sermant.registry.interceptors;

import io.sermant.registry.config.RegisterDynamicConfig;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.entity.MicroServiceInstance;
import io.sermant.registry.interceptors.cloud3.x.ZookeeperInstanceSupplierInterceptorTest;
import io.sermant.registry.services.RegisterCenterService;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.service.PluginServiceManager;

import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 * Server service discovery-ribbon
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class DynamicServerListInterceptorTest extends BaseRegistryTest<DynamicServerListInterceptor> {
    private final List<MicroServiceInstance> instanceList = new ArrayList<>();
    private final List<Server> originServers = new ArrayList<>();

    private final String serviceName = "test";

    @Mock
    private RegisterCenterService registerCenterService;

    @Mock
    private DynamicServerListLoadBalancer<Server> serverListLoadBalancer;

    @Mock
    private ServerList<Server> serverList;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(RegisterCenterService.class))
                .thenReturn(registerCenterService);
        final ZookeeperInstanceSupplierInterceptorTest test = new ZookeeperInstanceSupplierInterceptorTest();
        instanceList.add(test.buildInstance(8001));
        instanceList.add(test.buildInstance(8002));
        Mockito.when(registerCenterService.getServerList(serviceName)).thenReturn(instanceList);
        Mockito.when(serverListLoadBalancer.getFilter()).thenReturn(Mockito.mock(ServerListFilter.class));
        Mockito.when(serverListLoadBalancer.getServerListImpl()).thenReturn(serverList);
        Mockito.when(serverList.getUpdatedListOfServers()).thenReturn(originServers);
        String host = "localhost";
        originServers.add(new Server(host, 8003));
        originServers.add(new Server(host, 8004));
    }

    @Test
    public void doBefore() throws NoSuchMethodException {
        RegisterDynamicConfig.INSTANCE.setClose(false);
        RegisterContext.INSTANCE.setAvailable(true);
        final ExecuteContext context = interceptor.doBefore(buildContext(serverListLoadBalancer, null));
        Assert.assertTrue(context.isSkip());
        Mockito.verify(serverList, Mockito.times(1)).getUpdatedListOfServers();
        RegisterContext.INSTANCE.setAvailable(false);
        RegisterDynamicConfig.INSTANCE.setClose(true);
    }

    @Override
    protected DynamicServerListInterceptor getInterceptor() {
        return new DynamicServerListInterceptor();
    }
}
