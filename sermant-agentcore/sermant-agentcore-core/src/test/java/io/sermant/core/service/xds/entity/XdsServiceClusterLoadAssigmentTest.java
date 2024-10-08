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

package io.sermant.core.service.xds.entity;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * XdsServiceClusterInstanceTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class XdsServiceClusterLoadAssigmentTest {
    private static XdsServiceClusterLoadAssigment serviceClusterInstance;

    private static XdsClusterLoadAssigment clusterInstance1;

    private static XdsClusterLoadAssigment clusterInstance2;

    private static ServiceInstance serviceInstance1;

    private static ServiceInstance serviceInstance2;

    private static ServiceInstance serviceInstance3;

    private static Map<String, XdsClusterLoadAssigment> clusterInstances;

    @BeforeClass
    public static void setUp() {
        serviceClusterInstance = new XdsServiceClusterLoadAssigment();

        serviceInstance1 = new TestServiceInstance("outbound|8080||serviceA.default.svc.cluster.local", "serviceA",
                "localhost", 8080);
        serviceInstance2 = new TestServiceInstance("outbound|8080||serviceA.default.svc.cluster.local", "serviceA",
                "localhost", 9090);
        serviceInstance3 = new TestServiceInstance("outbound|8080|subset1|serviceA.default.svc.cluster.local",
                "serviceA",
                "localhost", 7070);

        clusterInstance1 = new XdsClusterLoadAssigment();
        clusterInstance2 = new XdsClusterLoadAssigment();

        Map<XdsLocality, Set<ServiceInstance>> localityInstances1 = new HashMap<>();
        XdsLocality locality1 = new XdsLocality();
        locality1.setRegion("region1");
        locality1.setZone("zone1");
        locality1.setSubZone("subZone1");
        localityInstances1.put(locality1, new HashSet<>(Arrays.asList(serviceInstance1, serviceInstance2)));

        Map<XdsLocality, Set<ServiceInstance>> localityInstances2 = new HashMap<>();
        XdsLocality locality2 = new XdsLocality();
        locality2.setRegion("region2");
        locality2.setZone("zone2");
        locality2.setSubZone("subZone2");
        localityInstances2.put(locality2, new HashSet<>(Collections.singletonList(serviceInstance3)));

        clusterInstance1.setLocalityInstances(localityInstances1);
        clusterInstance2.setLocalityInstances(localityInstances2);

        Map<String, XdsClusterLoadAssigment> clusterInstances = new HashMap<>();
        clusterInstances.put("outbound|8080||serviceA.default.svc.cluster.local", clusterInstance1);
        clusterInstances.put("outbound|8080|subset1|serviceA.default.svc.cluster.local", clusterInstance2);

        serviceClusterInstance.setClusterLoadAssigments(clusterInstances);
        serviceClusterInstance.setBaseClusterName("outbound|8080||serviceA.default.svc.cluster.local");
    }

    @Test
    public void testGetClusterInstances() {
        Map<String, XdsClusterLoadAssigment> clusterInstances = serviceClusterInstance.getClusterLoadAssigments();
        Assert.assertNotNull(clusterInstances);
        Assert.assertEquals(2, clusterInstances.size());
        Assert.assertTrue(clusterInstances.containsKey("outbound|8080||serviceA.default.svc.cluster.local"));
        Assert.assertTrue(clusterInstances.containsKey("outbound|8080|subset1|serviceA.default.svc.cluster.local"));
    }

    @Test
    public void testGetBaseClusterName() {
        Assert.assertEquals("outbound|8080||serviceA.default.svc.cluster.local",
                serviceClusterInstance.getBaseClusterName());
    }

    @Test
    public void testGetServiceInstance() {
        Set<ServiceInstance> serviceInstances = serviceClusterInstance.getServiceInstance();
        Assert.assertNotNull(serviceInstances);
        Assert.assertEquals(2, serviceInstances.size());
        Assert.assertTrue(serviceInstances.contains(serviceInstance1));
        Assert.assertTrue(serviceInstances.contains(serviceInstance2));
    }

    @Test
    public void testGetServiceInstanceWithClusterName() {
        Set<ServiceInstance> serviceInstances = serviceClusterInstance
                .getServiceInstance("outbound|8080|subset1|serviceA.default.svc.cluster.local");
        Assert.assertNotNull(serviceInstances);
        Assert.assertEquals(1, serviceInstances.size());
        Assert.assertTrue(serviceInstances.contains(serviceInstance3));
    }

    @Test
    public void testGetServiceInstanceWithNonExistentCluster() {
        Set<ServiceInstance> serviceInstances = serviceClusterInstance.getServiceInstance("nonexistentCluster");
        Assert.assertNotNull(serviceInstances);
        Assert.assertTrue(serviceInstances.isEmpty());
    }

    @Test
    public void testGetXdsClusterInstance() {
        XdsClusterLoadAssigment clusterInstance = serviceClusterInstance
                .getXdsClusterLoadAssigment("outbound|8080||serviceA.default.svc.cluster.local");
        Assert.assertNotNull(clusterInstance);
        Assert.assertEquals(clusterInstance1, clusterInstance);

        XdsClusterLoadAssigment nonExistentInstance = serviceClusterInstance.getXdsClusterLoadAssigment("nonexistentCluster");
        Assert.assertNull(nonExistentInstance);
    }
}
