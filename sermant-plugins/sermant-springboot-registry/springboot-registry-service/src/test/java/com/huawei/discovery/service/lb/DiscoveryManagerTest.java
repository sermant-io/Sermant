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

package com.huawei.discovery.service.lb;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.lb.discovery.zk.ZkClient;
import com.huawei.discovery.service.lb.discovery.zk.ZkService34;
import com.huawei.discovery.service.lb.rule.BaseTest;
import com.huawei.discovery.service.lb.rule.RandomLoadbalancer;
import com.huawei.discovery.service.lb.rule.RoundRobinLoadbalancer;
import com.huawei.discovery.service.lb.utils.CommonUtils;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.curator.framework.state.ConnectionState;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 服务发现测试
 *
 * @author zhouss
 * @since 2022-10-10
 */
public class DiscoveryManagerTest extends BaseTest {
    @Mock
    private ZkService34 zkService34;

    private final String serviceName = "discovery";

    @Override
    public void setUp() {
        super.setUp();
        final ZkClient client = getClient();
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(ZkService34.class))
                .thenReturn(zkService34);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(ZkClient.class))
                .thenReturn(client);
        start();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        // 重置状态
        final Optional<Object> isStarted = ReflectUtils.getFieldValue(DiscoveryManager.INSTANCE, "isStarted");
        Assert.assertTrue(isStarted.isPresent() && isStarted.get() instanceof AtomicBoolean);
        ((AtomicBoolean) isStarted.get()).set(false);
    }

    private void start() {
        DiscoveryManager.INSTANCE.start();
        Mockito.verify(zkService34, Mockito.times(1)).init();
    }

    @Test
    public void registry() {
        final ServiceInstance serviceInstance = CommonUtils.buildInstance(serviceName, 9090);
        Mockito.when(zkService34.registry(serviceInstance)).thenReturn(true);
        DiscoveryManager.INSTANCE.registry(serviceInstance);
        Mockito.verify(zkService34, Mockito.times(1)).registry(serviceInstance);
    }

    @Test
    public void choose() {
        final List<ServiceInstance> serviceInstances = mockInstances();
        final Optional<ServiceInstance> choose = DiscoveryManager.INSTANCE.choose(serviceName);
        Assert.assertTrue(choose.isPresent());
        Assert.assertTrue(serviceInstances.contains(choose.get()));
    }

    private List<ServiceInstance> mockInstances() {
        final List<ServiceInstance> serviceInstances = Arrays
                .asList(CommonUtils.buildInstance(serviceName, 8888), CommonUtils.buildInstance(serviceName, 8989));
        try {
            Mockito.when(zkService34.getInstances(serviceName)).thenReturn(serviceInstances);
        } catch (com.huawei.discovery.service.ex.QueryInstanceException e) {
            e.printStackTrace();
        }
        return serviceInstances;
    }

    @Test
    public void testChoose() {
        final List<ServiceInstance> serviceInstances = mockInstances();
        final Optional<ServiceInstance> choose = DiscoveryManager.INSTANCE
                .choose(serviceName, new RoundRobinLoadbalancer());
        Assert.assertTrue(choose.isPresent());
        Assert.assertTrue(serviceInstances.contains(choose.get()));
    }

    @Test
    public void testChoose1() {
        final List<ServiceInstance> serviceInstances = mockInstances();
        AtomicBoolean isFilter = new AtomicBoolean(false);
        final Optional<ServiceInstance> choose = DiscoveryManager.INSTANCE
                .choose(serviceName, new RandomLoadbalancer(), (curName, instances) -> {
                    isFilter.set(true);
                    return instances;
                });
        Assert.assertTrue(choose.isPresent());
        Assert.assertTrue(serviceInstances.contains(choose.get()));
        Assert.assertTrue(isFilter.get());
    }

    @Test
    public void stop() throws IOException {
        DiscoveryManager.INSTANCE.stop();
        Mockito.verify(zkService34, Mockito.times(1)).close();
        Mockito.verify(zkService34, Mockito.times(1)).unRegistry();
    }
}
