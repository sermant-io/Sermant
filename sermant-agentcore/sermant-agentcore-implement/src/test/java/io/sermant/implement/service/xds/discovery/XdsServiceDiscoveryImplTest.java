/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.xds.discovery;

import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.implement.service.xds.BaseXdsTest;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.entity.XdsServiceInstance;
import io.sermant.implement.service.xds.handler.XdsServiceDiscoveryListenerImpl;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

/**
 * XdsServiceDiscoveryImplTest
 *
 * @author daizhenyu
 * @since 2024-05-24
 **/
public class XdsServiceDiscoveryImplTest extends BaseXdsTest {
    private static String serviceName = "serviceA";

    private static XdsServiceDiscoveryImpl xdsServiceDiscovery;

    @BeforeClass
    public static void setUp() {
        xdsServiceDiscovery = new XdsServiceDiscoveryImpl(client);
    }

    @AfterClass
    public static void tearDown() {
        XdsDataCache.removeServiceInstance(serviceName);
        XdsDataCache.removeRequestObserver(serviceName);
        XdsDataCache.removeServiceDiscoveryListeners(serviceName);
    }

    @Test
    public void getServiceInstance() {
        // clear data
        XdsDataCache.removeServiceInstance(serviceName);
        XdsDataCache.removeRequestObserver(serviceName);

        // no service instance in cache
        Set<ServiceInstance> result = xdsServiceDiscovery.getServiceInstance(serviceName);
        Assert.assertNotNull(XdsDataCache.getRequestObserver(serviceName));

        // service instance in cache
        Set<ServiceInstance> instances = new HashSet<>();
        instances.add(new XdsServiceInstance());
        XdsDataCache.updateServiceInstance(serviceName, instances);
        result = xdsServiceDiscovery.getServiceInstance(serviceName);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void subscribeServiceInstance() {
        // clear data
        XdsDataCache.removeServiceInstance(serviceName);
        XdsDataCache.removeServiceDiscoveryListeners(serviceName);
        XdsDataCache.removeRequestObserver(serviceName);

        // no service instance in cache
        XdsServiceDiscoveryListenerImpl xdsServiceDiscoveryListener = new XdsServiceDiscoveryListenerImpl();
        xdsServiceDiscovery.subscribeServiceInstance(serviceName, xdsServiceDiscoveryListener);
        Assert.assertEquals(1, XdsDataCache.getServiceDiscoveryListeners(serviceName).size());
        Assert.assertEquals(0, xdsServiceDiscoveryListener.getCount());

        // service instance in cache
        Set<ServiceInstance> instances = new HashSet<>();
        instances.add(new XdsServiceInstance());
        XdsDataCache.updateServiceInstance(serviceName, instances);
        xdsServiceDiscovery.subscribeServiceInstance(serviceName, xdsServiceDiscoveryListener);
        Assert.assertEquals(1, xdsServiceDiscoveryListener.getCount());
    }
}