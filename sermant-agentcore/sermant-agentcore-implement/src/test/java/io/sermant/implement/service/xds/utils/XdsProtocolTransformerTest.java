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

package io.sermant.implement.service.xds.utils;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;
import io.sermant.core.service.xds.entity.ServiceInstance;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XdsProtocolTransformer UT
 *
 * @author daizhenyu
 * @since 2024-05-11
 **/
public class XdsProtocolTransformerTest {

    @Test
    public void testGetService2ClusterMapping() {
        List<Cluster> clusters = Arrays.asList(
                null,
                createCluster("outbound|8080||serviceA.default.svc.cluster.local"),
                createCluster("outbound|8080|subset1|serviceB.default.svc.cluster.local"),
                createCluster("outbound|8080|subset2|serviceB.default.svc.cluster.local"),
                createCluster("outbound|8080|serviceC.default.svc.cluster.local"),
                createCluster(null)
        );

        Map<String, Set<String>> result = XdsProtocolTransformer.getService2ClusterMapping(clusters);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsKey("serviceA"));
        Assert.assertTrue(result.containsKey("serviceB"));
        Assert.assertEquals(1, result.get("serviceA").size());
        Assert.assertEquals(2, result.get("serviceB").size());
    }

    @Test
    public void testGetServiceInstances() {
        List<ClusterLoadAssignment> assignments = Arrays.asList(
                null,
                createLoadAssignment("outbound|8080|subset1|serviceB.default.svc.cluster.local"),
                createLoadAssignment("outbound|8080|subset2|serviceB.default.svc.cluster.local"),
                createLoadAssignment("outbound|8080|serviceB.default.svc.cluster.local")
        );

        Set<ServiceInstance> result = XdsProtocolTransformer.getServiceInstances(assignments);
        Assert.assertEquals(2, result.size());
        Iterator<ServiceInstance> iterator = result.iterator();
        while (iterator.hasNext()) {
            ServiceInstance next = iterator.next();
            Assert.assertEquals("serviceB", next.getServiceName());
        }
    }

    private Cluster createCluster(String name) {
        Cluster.Builder builder = Cluster.newBuilder();
        if (name != null) {
            builder.setName(name);
        }
        return builder.build();
    }

    private ClusterLoadAssignment createLoadAssignment(String clusterName) {
        ClusterLoadAssignment.Builder assignmentBuilder = ClusterLoadAssignment.newBuilder();

        LocalityLbEndpoints.Builder localityBuilder = LocalityLbEndpoints.newBuilder();
        LbEndpoint.Builder endpointBuilder = LbEndpoint.newBuilder();
        localityBuilder.addLbEndpoints(endpointBuilder.build());
        assignmentBuilder.setClusterName(clusterName);
        assignmentBuilder.addEndpoints(localityBuilder.build());

        return assignmentBuilder.build();
    }
}
