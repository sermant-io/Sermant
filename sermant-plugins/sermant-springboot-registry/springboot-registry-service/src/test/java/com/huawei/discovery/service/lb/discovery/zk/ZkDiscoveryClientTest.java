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

import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.service.ex.QueryInstanceException;
import com.huawei.discovery.service.lb.LbConstants;
import com.huawei.discovery.service.lb.discovery.ServiceDiscoveryClient;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * zk client testing
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class ZkDiscoveryClientTest {
    private final LbConfig lbConfig = new LbConfig();

    protected MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private ServiceDiscoveryClient zkDiscoveryClient;

    private ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

    private final String serviceName = "zk";

    private org.apache.curator.x.discovery.ServiceInstance<ZookeeperInstance> instance;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(lbConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class))
                .thenReturn(new DiscoveryPluginConfig());
        pluginServiceManagerMockedStatic = Mockito
                .mockStatic(PluginServiceManager.class);
        instance = new org.apache.curator.x.discovery.ServiceInstance<>(serviceName, UUID.randomUUID().toString()
                        , "localhost", 8080, null,
                        new ZookeeperInstance("localhost:8080", serviceName,
                                Collections.singletonMap(LbConstants.SERMANT_DISCOVERY, "zk-node")),
                        System.currentTimeMillis(), ServiceType.DYNAMIC, new UriSpec(lbConfig.getZkUriSpec()));
        zkDiscoveryClient = getZkDiscoveryClient();
        zkDiscoveryClient.init();
        mockState();
        serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        ReflectUtils.setFieldValue(zkDiscoveryClient, "serviceDiscovery", serviceDiscovery);
        ReflectUtils.setFieldValue(zkDiscoveryClient, "instance", instance);
    }

    private void mockState() {
        final ZkClient zkClient = new ZkClient();
        zkClient.start();
        final Optional<Object> zkState = ReflectUtils.getFieldValue(zkClient, "zkState");
        Assert.assertTrue(zkState.isPresent() && zkState.get() instanceof AtomicReference);
        ((AtomicReference<ConnectionState>) zkState.get()).set(ConnectionState.CONNECTED);
        ReflectUtils.setFieldValue(zkDiscoveryClient, "zkClient", zkClient);
        final Optional<Object> isStarted = ReflectUtils.getFieldValue(zkDiscoveryClient, "isStarted");
        Assert.assertTrue(isStarted.isPresent() && isStarted.get() instanceof AtomicBoolean);
        ((AtomicBoolean) isStarted.get()).set(true);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        pluginServiceManagerMockedStatic.close();
    }

    @Test
    public void registry() throws Exception {
        final DefaultServiceInstance instance = new DefaultServiceInstance("localhost", "127.0.0.1", 8080,
                Collections.emptyMap(), serviceName);
        final boolean result = zkDiscoveryClient.registry(instance);
        Assert.assertTrue(result);
        Mockito.verify(serviceDiscovery, Mockito.times(1)).registerService(Mockito.any());
    }

    @Test(expected = QueryInstanceException.class)
    public void getInstances() throws Exception {
        Mockito.when(serviceDiscovery.queryForInstances(serviceName)).thenReturn(Collections.emptyList());
        Assert.assertEquals(Collections.emptyList(), zkDiscoveryClient.getInstances(serviceName));
        Mockito.when(serviceDiscovery.queryForInstances(serviceName)).thenReturn(Collections.singletonList(instance));
        Assert.assertEquals(zkDiscoveryClient.getInstances(serviceName).size(), 1);

        // The simulation throws an exception
        Mockito.when(serviceDiscovery.queryForInstances(serviceName)).thenThrow(new IllegalStateException("wrong"));
        Assert.assertEquals(Collections.emptyList(), zkDiscoveryClient.getInstances(serviceName));
    }

    @Test
    public void getServices() throws Exception {
        final List<String> serviceNames = Arrays.asList("test1", "test2");
        Mockito.when(serviceDiscovery.queryForNames()).thenReturn(serviceNames);
        Assert.assertEquals(zkDiscoveryClient.getServices(), serviceNames);

        // The simulation throws an exception
        Mockito.when(serviceDiscovery.queryForNames()).thenThrow(new IllegalStateException("wrong"));
        Assert.assertEquals(Collections.emptyList(), zkDiscoveryClient.getServices());
    }

    @Test
    public void unRegistry() throws Exception {
        zkDiscoveryClient.unRegistry();
        Mockito.verify(serviceDiscovery, Mockito.times(1)).unregisterService(Mockito.any());

        // The simulation throws an exception
        Mockito.doThrow(new IllegalStateException("wrong")).when(serviceDiscovery).unregisterService(Mockito.any());
        zkDiscoveryClient.unRegistry();
    }

    @Test
    public void close() throws IOException {
        zkDiscoveryClient.close();
        Mockito.doThrow(new IOException("wrong")).when(serviceDiscovery).close();
        Mockito.verify(serviceDiscovery, Mockito.times(1)).close();
    }

    /**
     * Get the client
     *
     * @return ZkDiscoveryClient
     */
    protected ServiceDiscoveryClient getZkDiscoveryClient() {
        return new ZkDiscoveryClient();
    }
}
