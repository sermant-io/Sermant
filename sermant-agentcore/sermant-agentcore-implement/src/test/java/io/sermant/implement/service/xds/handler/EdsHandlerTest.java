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

import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.Endpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;
import io.sermant.implement.service.xds.BaseXdsTest;
import io.sermant.implement.service.xds.cache.XdsDataCache;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * EdsHandler UT
 *
 * @author daizhenyu
 * @since 2024-05-25
 **/
public class EdsHandlerTest extends BaseXdsTest {
    private static String serviceName = "serviceA";

    private String clusterName = "outbound|8080||serviceA.default.svc.cluster.local";

    private static EdsHandler handler;

    @BeforeClass
    public static void setUp() throws Exception {
        handler = new EdsHandler(client);
        Mockito.doReturn(requestStreamObserver).when(client)
                .getDiscoveryRequestObserver(handler.getResponseStreamObserver(serviceName, null));
        XdsDataCache.addServiceDiscoveryListener(serviceName, new XdsServiceDiscoveryListenerImpl());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Mockito.clearAllCaches();
        XdsDataCache.removeRequestObserver(serviceName);
        XdsDataCache.removeServiceDiscoveryListeners(serviceName);
        XdsDataCache.removeServiceInstance(serviceName);
    }

    @Test
    public void testHandleResponse() {
        handler.subscribe(serviceName, null);

        // first update service instance of serviceA
        handler.handleResponse(serviceName, buildDiscoveryResponse("127.0.0.1", 8080));
        assertHandleResponseLogic(1, 1, "127.0.0.1", 8080);

        // second update service instance of serviceA, service instance not changed
        handler.handleResponse(serviceName, buildDiscoveryResponse("127.0.0.1", 8080));
        assertHandleResponseLogic(1, 1, "127.0.0.1", 8080);

        // third update service instance of serviceA, service instance changed
        handler.handleResponse(serviceName, buildDiscoveryResponse("127.0.0.1", 8082));
        assertHandleResponseLogic(1, 2, "127.0.0.1", 8082);
    }

    private void assertHandleResponseLogic(int expectedInstanceSize, int expectedListenerStatus, String expectedIp,
            int expectedPort) {
        Set<ServiceInstance> serviceInstances = XdsDataCache.getServiceInstance(serviceName);
        List<XdsServiceDiscoveryListener> listeners = XdsDataCache.getServiceDiscoveryListeners(serviceName);
        XdsServiceDiscoveryListenerImpl listenerImpl = (XdsServiceDiscoveryListenerImpl) listeners.get(0);
        Assert.assertEquals(expectedInstanceSize, serviceInstances.size());
        Assert.assertEquals(expectedListenerStatus, listenerImpl.getCount());
        Iterator<ServiceInstance> iterator = serviceInstances.iterator();
        if (iterator.hasNext()) {
            ServiceInstance next = iterator.next();
            Assert.assertEquals(expectedIp, next.getHost());
            Assert.assertEquals(expectedPort, next.getPort());
        }
    }

    private DiscoveryResponse buildDiscoveryResponse(String ip, int port) {
        List<Any> resources = new ArrayList<>();
        resources.add(Any.pack(createLoadAssignment(clusterName, ip, port)));
        return DiscoveryResponse.newBuilder().addAllResources(resources).build();
    }

    private ClusterLoadAssignment createLoadAssignment(String clusterName, String ip, int port) {
        ClusterLoadAssignment.Builder assignmentBuilder = ClusterLoadAssignment.newBuilder();
        LocalityLbEndpoints.Builder localityBuilder = LocalityLbEndpoints.newBuilder();
        LbEndpoint.Builder lbEndpointBuilder = LbEndpoint.newBuilder();
        Endpoint.Builder endpointBuilder = Endpoint.newBuilder();
        Address.Builder addressBuilder = Address.newBuilder();
        SocketAddress.Builder sockAddressBuilder = SocketAddress.newBuilder();

        sockAddressBuilder.setAddress(ip);
        sockAddressBuilder.setPortValue(port);
        addressBuilder.setSocketAddress(sockAddressBuilder.build());
        endpointBuilder.setAddress(addressBuilder.build());
        lbEndpointBuilder.setEndpoint(endpointBuilder.build());
        localityBuilder.addLbEndpoints(lbEndpointBuilder.build());
        assignmentBuilder.setClusterName(clusterName);
        assignmentBuilder.addEndpoints(localityBuilder.build());

        return assignmentBuilder.build();
    }
}