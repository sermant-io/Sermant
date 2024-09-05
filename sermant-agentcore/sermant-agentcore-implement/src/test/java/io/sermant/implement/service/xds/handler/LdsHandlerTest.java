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

import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.Rds;
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
import java.util.List;
import java.util.Set;

/**
 * LdsHandlerTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class LdsHandlerTest extends BaseXdsTest {
    private static LdsHandler handler;

    @BeforeClass
    public static void setUp() {
        handler = new LdsHandler(client);
        Mockito.doReturn(requestStreamObserver).when(client).getDiscoveryRequestObserver(handler
                .getResponseStreamObserver(XdsConstant.LDS_ALL_RESOURCE, null));
        XdsDataCache.updateRequestObserver(XdsConstant.RDS_ALL_RESOURCE, requestStreamObserver);
    }

    @AfterClass
    public static void tearDown() {
        Mockito.clearAllCaches();
        XdsDataCache.removeRequestObserver(XdsConstant.LDS_ALL_RESOURCE);
        XdsDataCache.removeRequestObserver(XdsConstant.RDS_ALL_RESOURCE);
        XdsDataCache.updateHttpConnectionManagers(new ArrayList<>());
    }

    @Test
    public void testHandleResponse() {
        handler.subscribe(XdsConstant.LDS_ALL_RESOURCE, null);

        // listener is empty
        handler.handleResponse(XdsConstant.LDS_ALL_RESOURCE,
                DiscoveryResponse.newBuilder().addAllResources(new ArrayList<>()).build());
        Assert.assertEquals(0, XdsDataCache.getRouteResources().size());

        // listener is not empty
        handler.handleResponse(XdsConstant.LDS_ALL_RESOURCE,
                buildDiscoveryResponse("test-listener", "test-routeConfig"));
        Set<String> routeResources = XdsDataCache.getRouteResources();
        Assert.assertEquals(1, routeResources.size());
        Assert.assertTrue(routeResources.contains("test-routeConfig"));
    }

    private Listener createListener(String listenerName, String routeConfigName) {
        HttpConnectionManager httpConnectionManager = createHttpConnectionManager(routeConfigName);
        Any httpConnectionManagerAny = Any.pack(httpConnectionManager);
        Filter httpFilter = Filter.newBuilder()
                .setTypedConfig(httpConnectionManagerAny)
                .build();
        FilterChain filterChain = FilterChain.newBuilder()
                .addFilters(httpFilter)
                .build();

        return Listener.newBuilder()
                .setName(listenerName)
                .addFilterChains(filterChain)
                .build();
    }

    private HttpConnectionManager createHttpConnectionManager(String routeConfigName) {
        HttpConnectionManager.Builder managerBuilder = HttpConnectionManager.newBuilder();
        Rds.Builder rdsBuilder = Rds.newBuilder();
        rdsBuilder.setRouteConfigName(routeConfigName);
        managerBuilder.setRds(rdsBuilder.build());
        return managerBuilder.build();
    }

    private DiscoveryResponse buildDiscoveryResponse(String listenerName, String routeConfigName) {
        Listener listener = createListener(listenerName, routeConfigName);
        List<Any> resources = new ArrayList<>();
        resources.add(Any.pack(listener));
        return DiscoveryResponse.newBuilder().addAllResources(resources).build();
    }
}