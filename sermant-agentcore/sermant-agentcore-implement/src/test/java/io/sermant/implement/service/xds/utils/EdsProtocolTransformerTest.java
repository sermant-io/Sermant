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

import io.envoyproxy.envoy.config.core.v3.Locality;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.service.xds.entity.XdsServiceClusterLoadAssigment;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * XdsProtocolTransformer UT
 *
 * @author daizhenyu
 * @since 2024-05-11
 **/
public class EdsProtocolTransformerTest {
    @Test
    public void testGetServiceInstances() {
        List<ClusterLoadAssignment> assignments = Arrays.asList(
                null,
                createLoadAssignment("outbound|8080|subset1|serviceB.default.svc.cluster.local"),
                createLoadAssignment("outbound|8080|subset2|serviceB.default.svc.cluster.local"),
                createLoadAssignment("outbound|8080|serviceB.default.svc.cluster.local")
        );

        XdsServiceClusterLoadAssigment result = EdsProtocolTransformer.getServiceInstances(assignments);
        Assert.assertEquals(2, result.getClusterLoadAssigments().size());
        Iterator<ServiceInstance> iterator = result.getServiceInstance().iterator();
        while (iterator.hasNext()) {
            ServiceInstance next = iterator.next();
            Assert.assertEquals("serviceB", next.getServiceName());
        }
        Assert.assertEquals(2, result.getClusterLoadAssigments().size());
        Assert.assertEquals("outbound|8080|subset1|serviceB.default.svc.cluster.local",
                result.getXdsClusterLoadAssigment("outbound|8080|subset1|serviceB.default.svc.cluster.local")
                        .getClusterName());
        Assert.assertEquals("serviceB",
                result.getXdsClusterLoadAssigment("outbound|8080|subset1|serviceB.default.svc.cluster.local")
                        .getServiceName());

        Map<XdsLocality, Set<ServiceInstance>> localityInstances = result
                .getXdsClusterLoadAssigment("outbound|8080|subset1|serviceB.default.svc.cluster.local")
                .getLocalityInstances();
        Assert.assertEquals(1,
                localityInstances.size());
        for (Entry<XdsLocality, Set<ServiceInstance>> xdsLocalitySetEntry : localityInstances.entrySet()) {
            Assert.assertEquals("test-region", xdsLocalitySetEntry.getKey().getRegion());
            Assert.assertEquals("test-zone", xdsLocalitySetEntry.getKey().getZone());
            Assert.assertEquals("test-subzone", xdsLocalitySetEntry.getKey().getSubZone());
        }
    }

    private ClusterLoadAssignment createLoadAssignment(String clusterName) {
        ClusterLoadAssignment.Builder assignmentBuilder = ClusterLoadAssignment.newBuilder();
        LbEndpoint.Builder endpointBuilder = LbEndpoint.newBuilder();
        Locality.Builder localityBuilder = Locality.newBuilder();
        localityBuilder.setRegion("test-region").setZone("test-zone").setSubZone("test-subzone");
        LocalityLbEndpoints.Builder localityEndpointBuilder = LocalityLbEndpoints.newBuilder();
        localityEndpointBuilder.addLbEndpoints(endpointBuilder.build()).setLocality(localityBuilder.build());
        assignmentBuilder.setClusterName(clusterName);
        assignmentBuilder.addEndpoints(localityEndpointBuilder.build());

        return assignmentBuilder.build();
    }
}
