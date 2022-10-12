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

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.service.lb.discovery.ServiceDiscoveryClient;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;

/**
 * 代理测试
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class ZkDiscoveryClientProxyTest {
    private final LbConfig lbConfig = new LbConfig();

    protected MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private ServiceDiscoveryClient zkDiscoveryClient;

    private ZkService34 zkService34;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(lbConfig);
        pluginServiceManagerMockedStatic = Mockito
                .mockStatic(PluginServiceManager.class);
        zkDiscoveryClient = new ZkDiscoveryClientProxy();
        zkService34 = Mockito.mock(ZkService34.class);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(ZkService34.class))
                .thenReturn(zkService34);
        zkDiscoveryClient.init();
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        pluginServiceManagerMockedStatic.close();
        Mockito.reset(zkService34);
    }

    @Test
    public void registry() {
        final DefaultServiceInstance instance = new DefaultServiceInstance("localhost", "127.0.0.1", 8080,
                Collections.emptyMap(), "zk");
        Mockito.when(zkService34.registry(instance)).thenReturn(true);
        final boolean registry = zkDiscoveryClient.registry(instance);
        Assert.assertTrue(registry);
    }

    @Test
    public void getInstances() {
        String serviceName = "test1";
        try {
            Mockito.when(zkService34.getInstances(serviceName)).thenReturn(Collections.emptyList());
        } catch (com.huawei.discovery.service.ex.QueryInstanceException e) {
            e.printStackTrace();
        }
        try {
            Assert.assertEquals(zkDiscoveryClient.getInstances(serviceName), Collections.emptyList());
        } catch (com.huawei.discovery.service.ex.QueryInstanceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getServices() {
        Mockito.when(zkService34.getServices()).thenReturn(Collections.singletonList("serviceName"));
        Assert.assertEquals(zkDiscoveryClient.getServices().size(), 1);
    }

    @Test
    public void unRegistry() {
        Mockito.when(zkService34.unRegistry()).thenReturn(true);
        Assert.assertTrue(zkDiscoveryClient.unRegistry());
    }

    @Test
    public void close() throws IOException {
        zkDiscoveryClient.close();
        Mockito.verify(zkService34, Mockito.times(1)).close();
    }
}
