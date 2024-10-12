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
import io.sermant.core.service.xds.entity.XdsClusterLoadAssigment;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;
import io.sermant.implement.service.xds.CommonDataGenerator;
import io.sermant.implement.service.xds.entity.XdsServiceInstance;
import io.sermant.implement.service.xds.handler.StreamObserverRequestImpl;
import io.sermant.implement.service.xds.handler.XdsServiceDiscoveryListenerImpl;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * XdsDataCache UT
 *
 * @author daizhenyu
 * @since 2024-05-24
 **/
public class XdsDataCacheTest {
    private static String BASE_CLUSTER_NAME = "outbound|8080||serviceA.default.svc.cluster.local";

    @Test
    public void testServiceInstance() {
        XdsServiceInstance instance = new XdsServiceInstance();
        Set<ServiceInstance> instanceSet = new HashSet<>();
        instanceSet.add(instance);
        XdsDataCache.updateServiceInstance("serviceA",
                CommonDataGenerator
                        .createXdsServiceClusterInstance(Arrays.asList(BASE_CLUSTER_NAME), BASE_CLUSTER_NAME));
        Set<ServiceInstance> serviceA = XdsDataCache.getServiceInstance("serviceA");
        Set<ServiceInstance> serviceB = XdsDataCache.getServiceInstance("serviceB");
        Assert.assertEquals(1, serviceA.size());
        Assert.assertEquals(0, serviceB.size());

        Optional<XdsClusterLoadAssigment> clusterServiceInstance = XdsDataCache
                .getClusterServiceInstance("serviceA", BASE_CLUSTER_NAME);
        Assert.assertTrue(clusterServiceInstance.isPresent());
        Map<XdsLocality, Set<ServiceInstance>> localityInstances = clusterServiceInstance.get().getLocalityInstances();
        Assert.assertEquals(1, localityInstances.size());
        Assert.assertEquals(1, localityInstances.get(new XdsLocality()).size());

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
        Assert.assertNotNull(XdsDataCache.getRequestObserver("serviceA"));

        XdsDataCache.removeRequestObserver("serviceA");
        Set<Entry<String, StreamObserver<DiscoveryRequest>>> requestObserversEntry = XdsDataCache
                .getRequestObserversEntry();
        Assert.assertEquals(0, requestObserversEntry.size());
    }

    @Test
    public void testServiceClusterMap() {
        Set<String> result;

        // serviceClusterMap is empty
        XdsDataCache.updateServiceClusterMap(new HashMap<>());
        result = XdsDataCache.getClustersByServiceName("serviceA");
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

        // serviceClusterMap is not empty, get un cached service
        XdsDataCache.updateServiceClusterMap(CommonDataGenerator
                .createServiceClusterMap("serviceA", BASE_CLUSTER_NAME));
        result = XdsDataCache.getClustersByServiceName("serviceB");
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

        // serviceClusterMap is not null, get cached service
        result = XdsDataCache.getClustersByServiceName("serviceA");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains(BASE_CLUSTER_NAME));

        // test lb
        Assert.assertEquals(XdsLbPolicy.RANDOM, XdsDataCache.getBaseLbPolicyOfService("serviceA"));
        Assert.assertEquals(XdsLbPolicy.RANDOM, XdsDataCache.getLbPolicyOfCluster("serviceA", BASE_CLUSTER_NAME));
        Assert.assertEquals(true, XdsDataCache.isLocalityLb("serviceA", BASE_CLUSTER_NAME));

        // clear data
        XdsDataCache.updateServiceClusterMap(new HashMap<>());
    }

    @Test
    public void testHttpConnectionManagers() {
        XdsDataCache.updateHttpConnectionManagers(
                CommonDataGenerator.createHttpConnectionManagers(Arrays.asList("route-test")));
        Set<String> routeResources = XdsDataCache.getRouteResources();
        Assert.assertEquals(1, routeResources.size());
        Assert.assertTrue(routeResources.contains("route-test"));

        // clear data
        XdsDataCache.updateHttpConnectionManagers(new ArrayList<>());
    }

    @Test
    public void testRouteConfigurations() {
        XdsDataCache.updateRouteConfigurations(CommonDataGenerator.createRouteConfigurations());

        // get service route not in cache
        Assert.assertEquals(0, XdsDataCache.getServiceRoute("serviceC").size());

        // get service route in cache, but route is empty
        Assert.assertEquals(0, XdsDataCache.getServiceRoute("serviceB").size());

        // get service route in cache
        Assert.assertEquals(1, XdsDataCache.getServiceRoute("serviceA").size());
        Assert.assertEquals("test-route", XdsDataCache.getServiceRoute("serviceA").get(0).getName());

        // clear data
        XdsDataCache.updateRouteConfigurations(new ArrayList<>());
    }
}
