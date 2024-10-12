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
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class XdsClusterLoadAssigmentTest {
    @Test
    public void testXdsClusterInstance() {
        XdsLocality locality = new XdsLocality();
        Set<ServiceInstance> instances = new HashSet<>();
        instances.add(new TestServiceInstance());
        Map<XdsLocality, Set<ServiceInstance>> localityInstances = new HashMap<>();
        localityInstances.put(locality, instances);
        XdsClusterLoadAssigment clusterInstance = new XdsClusterLoadAssigment();
        clusterInstance.setClusterName("outbound|8080||serviceA.default.svc.cluster.local");
        clusterInstance.setServiceName("serviceA");
        clusterInstance.setLocalityInstances(localityInstances);
        Assert.assertEquals("serviceA", clusterInstance.getServiceName());
        Assert.assertEquals("outbound|8080||serviceA.default.svc.cluster.local", clusterInstance.getClusterName());
        Assert.assertEquals(localityInstances, clusterInstance.getLocalityInstances());
    }
}
