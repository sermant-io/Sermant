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

package io.sermant.implement.service.xds.cache;

import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.grpc.stub.StreamObserver;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;
import io.sermant.implement.service.xds.entity.XdsServiceInstance;
import io.sermant.implement.service.xds.handler.StreamObserverRequestImpl;
import io.sermant.implement.service.xds.handler.XdsServiceDiscoveryListenerImpl;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XdsDataCache UT
 *
 * @author daizhenyu
 * @since 2024-05-24
 **/
public class XdsDataCacheTest {
    @Test
    public void testServiceInstance() {
        XdsServiceInstance instance = new XdsServiceInstance();
        Set<ServiceInstance> instanceSet = new HashSet<>();
        instanceSet.add(instance);
        XdsDataCache.updateServiceInstance("serviceA", instanceSet);
        Set<ServiceInstance> serviceA = XdsDataCache.getServiceInstance("serviceA");
        Set<ServiceInstance> serviceB = XdsDataCache.getServiceInstance("serviceB");
        Assert.assertEquals(1, serviceA.size());
        Assert.assertEquals(0, serviceB.size());
        XdsDataCache.removeServiceInstance("serviceA");
        serviceA = XdsDataCache.getServiceInstance("serviceA");
        Assert.assertEquals(0, serviceA.size());
    }

    @Test
    public void testServiceListener() {
        // clean data
        XdsDataCache.removeServiceDiscoveryListeners("serviceA");

        XdsServiceDiscoveryListener listener = new XdsServiceDiscoveryListenerImpl();
        XdsDataCache.addServiceDiscoveryListener("serviceA", listener);
        List<XdsServiceDiscoveryListener> listenerA = XdsDataCache.getServiceDiscoveryListeners("serviceA");
        List<XdsServiceDiscoveryListener> listenerB = XdsDataCache.getServiceDiscoveryListeners("serviceB");
        Assert.assertEquals(1, listenerA.size());
        Assert.assertEquals(0, listenerB.size());
        XdsDataCache.removeServiceDiscoveryListeners("serviceA");
        listenerA = XdsDataCache.getServiceDiscoveryListeners("serviceA");
        Assert.assertEquals(0, listenerA.size());
    }

    @Test
    public void testRequestObserver() {
        StreamObserver<DiscoveryRequest> requestObserver = new StreamObserverRequestImpl();
        XdsDataCache.updateRequestObserver("serviceA", requestObserver);
        Assert.assertTrue(XdsDataCache.isContainsRequestObserver("serviceA"));
    }

    @Test
    public void testGetClustersByServiceName() {
        Map<String, Set<String>> mapping = new HashMap<>();
        Set<String> clusters = new HashSet<>();
        clusters.add("cluster");
        mapping.put("serviceA", clusters);
        Set<String> result;

        // serviceNameMapping is empty
        XdsDataCache.updateServiceNameMapping(new HashMap<>());
        result = XdsDataCache.getClustersByServiceName("serviceA");
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

        // serviceNameMapping is not empty, get un cached service
        XdsDataCache.updateServiceNameMapping(mapping);
        result = XdsDataCache.getClustersByServiceName("serviceB");
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

        // serviceNameMapping is not null, get cached service
        XdsDataCache.updateServiceNameMapping(mapping);
        result = XdsDataCache.getClustersByServiceName("serviceA");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("cluster"));
    }
}