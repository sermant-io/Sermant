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

package io.sermant.implement.service.xds.handler;

import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.sermant.implement.service.xds.BaseXdsTest;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.env.XdsConstant;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author daizhenyu
 * @since 2024-05-24
 **/
public class CdsXdsTest extends BaseXdsTest {
    private static CdsHandler handler;

    private static String serviceName = "serviceA";

    @BeforeClass
    public static void setUp() {
        handler = new CdsHandler(client);
        Mockito.doReturn(requestStreamObserver).when(client).getDiscoveryRequestObserver(handler
                .getResponseStreamObserver(XdsConstant.CDS_ALL_RESOURCE, null));

        handler.subscribe(XdsConstant.CDS_ALL_RESOURCE, null);
        XdsDataCache.updateRequestObserver(serviceName, requestStreamObserver);
    }

    @AfterClass
    public static void tearDown() {
        Mockito.clearAllCaches();
        XdsDataCache.removeRequestObserver(serviceName);
    }

    @Test
    public void testHandleResponse() {
        // cluster is empty
        handler.handleResponse(XdsConstant.CDS_ALL_RESOURCE,
                buildDiscoveryResponse(new ArrayList<>()));
        Assert.assertEquals(0, XdsDataCache.getServiceNameMapping().size());

        // service with one cluster
        handler.handleResponse(XdsConstant.CDS_ALL_RESOURCE,
                buildDiscoveryResponse(Arrays.asList("outbound|8080||serviceA.default.svc.cluster.local")));
        Set<String> clusterNames = XdsDataCache.getClustersByServiceName(serviceName);
        Assert.assertNotNull(clusterNames);
        Assert.assertEquals(1, clusterNames.size());
        Assert.assertTrue(clusterNames.contains("outbound|8080||serviceA.default.svc.cluster.local"));

        // service with many cluster
        handler.handleResponse(XdsConstant.CDS_ALL_RESOURCE,
                buildDiscoveryResponse(Arrays.asList(
                        "outbound|8080|subset1|serviceA.default.svc.cluster.local",
                        "outbound|8080|subset2|serviceA.default.svc.cluster.local"
                )));
        clusterNames = XdsDataCache.getClustersByServiceName(serviceName);
        Assert.assertEquals(2, clusterNames.size());
        Assert.assertTrue(clusterNames.contains("outbound|8080|subset1|serviceA.default.svc.cluster.local"));
        Assert.assertTrue(clusterNames.contains("outbound|8080|subset2|serviceA.default.svc.cluster.local"));
    }

    private DiscoveryResponse buildDiscoveryResponse(List<String> clusterNames) {
        List<Any> resources = new ArrayList<>();
        for (String clusterName : clusterNames) {
            Cluster cluster = Cluster.newBuilder().setName(clusterName).build();
            resources.add(Any.pack(cluster));
        }
        return DiscoveryResponse.newBuilder().addAllResources(resources).build();
    }
}