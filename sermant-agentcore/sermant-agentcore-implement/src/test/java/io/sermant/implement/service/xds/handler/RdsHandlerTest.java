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

import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.implement.service.xds.BaseXdsTest;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.env.XdsConstant;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * RdsHandlerTest
 *
 * @author daizhenyu
 * @since 2024-08-26
 **/
public class RdsHandlerTest extends BaseXdsTest {
    private static RdsHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new RdsHandler(client);
        Mockito.doReturn(requestStreamObserver).when(client).getDiscoveryRequestObserver(handler
                .getResponseStreamObserver(XdsConstant.RDS_ALL_RESOURCE, null));
    }

    @After
    public void tearDown() throws Exception {
        Mockito.clearAllCaches();
        XdsDataCache.removeRequestObserver(XdsConstant.RDS_ALL_RESOURCE);
        XdsDataCache.updateRouteConfigurations(new ArrayList<>());
    }

    @Test
    public void testHandleResponse() {
        handler.subscribe(XdsConstant.RDS_ALL_RESOURCE, null);

        // routeConfiguration is empty
        handler.handleResponse(XdsConstant.RDS_ALL_RESOURCE,
                DiscoveryResponse.newBuilder().addAllResources(new ArrayList<>()).build());
        Assert.assertEquals(0, XdsDataCache.getRouteConfigurations().size());

        // routeConfiguration is not empty
        handler.handleResponse(XdsConstant.RDS_ALL_RESOURCE,
                buildDiscoveryResponse("serviceA.example.com", "test-route",
                        "test-routeConfig"));
        List<XdsRoute> route = XdsDataCache.getServiceRoute("serviceA");
        Assert.assertEquals(1, route.size());
        Assert.assertEquals("test-route", route.get(0).getName());
    }

    private DiscoveryResponse buildDiscoveryResponse(String virtualHostName, String routeName, String routeConfigName) {
        RouteConfiguration configuration = createRouteConfiguration(virtualHostName, routeName, routeConfigName);
        List<Any> resources = new ArrayList<>();
        resources.add(Any.pack(configuration));
        return DiscoveryResponse.newBuilder().addAllResources(resources).build();
    }

    private RouteConfiguration createRouteConfiguration(String virtualHostName, String routeName,
            String routeConfigName) {
        return RouteConfiguration.newBuilder()
                .setName(routeConfigName)
                .addVirtualHosts(createVirtualHost(virtualHostName, routeName))
                .build();
    }

    private VirtualHost createVirtualHost(String virtualHostName, String routeName) {
        return VirtualHost.newBuilder()
                .setName(virtualHostName)
                .addRoutes(Route.newBuilder().setName(routeName).build())
                .build();
    }
}