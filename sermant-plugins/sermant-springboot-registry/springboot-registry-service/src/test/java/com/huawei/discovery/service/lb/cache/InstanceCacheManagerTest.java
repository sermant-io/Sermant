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

package com.huawei.discovery.service.lb.cache;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.lb.discovery.ServiceDiscoveryClient;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 缓存测试
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class InstanceCacheManagerTest {
    private final List<ServiceInstance> instances = new ArrayList<>();

    private final String serviceName = "test";

    private final LbConfig lbConfig = new LbConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        instances.add(new DefaultServiceInstance("localhost", "127.0.0.1", 8080, Collections.emptyMap(),
                serviceName));
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(lbConfig);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void getInstances() {
        final ServiceDiscoveryClient client = Mockito.mock(ServiceDiscoveryClient.class);
        try {
            Mockito.when(client.getInstances(serviceName)).thenReturn(instances);
        } catch (com.huawei.discovery.service.ex.QueryInstanceException e) {
            e.printStackTrace();
        }
        final InstanceCacheManager instanceCacheManager = new InstanceCacheManager(client, null);
        Assert.assertEquals(instanceCacheManager.getInstances(serviceName), instances);

        final Optional<Object> tryUpdateInstances = ReflectUtils
                .invokeMethod(instanceCacheManager, "tryUpdateInstances", new Class[]{String.class},
                        new Object[]{serviceName});
        Assert.assertTrue(tryUpdateInstances.isPresent());
        Assert.assertEquals(tryUpdateInstances.get(), instances);

        final Optional<Object> createCache = ReflectUtils
                .invokeMethod(instanceCacheManager, "createCache", new Class[]{String.class},
                        new Object[]{serviceName});
        Assert.assertTrue(createCache.isPresent() && createCache.get() instanceof InstanceCache);
        Assert.assertEquals(((InstanceCache) createCache.get()).getInstances(), instances);
        Assert.assertEquals(((InstanceCache) createCache.get()).getServiceName(), serviceName);

        try {
            Mockito.when(client.getInstances(serviceName)).thenReturn(Collections.emptyList());
        } catch (com.huawei.discovery.service.ex.QueryInstanceException e) {
            e.printStackTrace();
        }
        final InstanceCacheManager emptyManager = new InstanceCacheManager(client, null);
        final Optional<Object> emptyCache = ReflectUtils
                .invokeMethod(emptyManager, "createCache", new Class[]{String.class},
                        new Object[]{serviceName});
        Assert.assertTrue(emptyCache.isPresent() && emptyCache.get() instanceof InstanceCache);
        Assert.assertEquals(((InstanceCache) emptyCache.get()).getInstances(), Collections.emptyList());
        Assert.assertEquals(((InstanceCache) emptyCache.get()).getServiceName(), serviceName);
    }
}
