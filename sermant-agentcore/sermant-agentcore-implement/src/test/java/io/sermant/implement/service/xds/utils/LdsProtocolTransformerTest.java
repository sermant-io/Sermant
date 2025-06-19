/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.Rds;
import io.sermant.core.service.xds.entity.XdsHttpConnectionManager;
import io.sermant.core.service.xds.entity.XdsJwtRule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LdsProtocolTransformerTest
 *
 * @author daizhenyu
 * @since 2024-08-23
 **/
@RunWith(MockitoJUnitRunner.class)
public class LdsProtocolTransformerTest {
    // Mock objects
    @Mock
    private Listener listener;

    @Mock
    private Listener nonVirtualListener;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Filter nonHttpFilter;

    @Mock
    private HttpFilter jwtFilter;

    @Mock
    private HttpFilter otherFilter;

    @Mock
    private Any typedConfig;

    private static final String VIRTUAL_INBOUND = "virtualInbound";

    private static final String HTTP_CONNECTION_MANAGER = "http_connection_manager";

    private static final String OTHER_LISTENER_NAME = "other_listener";

    private static final String LDS_JWT_FILTER = "envoy.filters.http.jwt_authn";

    private static final String OTHER_FILTER = "other.filter";

    @Test
    public void getHttpConnectionManager() {
        List<Listener> listeners = Arrays.asList(
                null,
                createListener("testA", "test-routeA"),
                createListener("testB", "test-routeB"),
                createListener("testC", null)
        );
        List<XdsHttpConnectionManager> httpConnectionManagers = LdsProtocolTransformer
                .getHttpConnectionManager(listeners);
        assertEquals(2, httpConnectionManagers.size());
        Set<String> actualRouteConfigNames = new HashSet<>();
        actualRouteConfigNames.add(httpConnectionManagers.get(0).getRouteConfigName());
        actualRouteConfigNames.add(httpConnectionManagers.get(1).getRouteConfigName());

        Set<String> expectRouteConfigNames = new HashSet<>();
        expectRouteConfigNames.add("test-routeA");
        expectRouteConfigNames.add("test-routeB");
        assertEquals(expectRouteConfigNames, actualRouteConfigNames);
    }

    private Listener createListener(String listenerName, String routeName) {
        Filter httpFilter;
        if (routeName != null) {
            HttpConnectionManager httpConnectionManager = createHttpConnectionManager(routeName);
            Any httpConnectionManagerAny = Any.pack(httpConnectionManager);
            httpFilter = Filter.newBuilder()
                    .setName("envoy.filters.network.http_connection_manager")
                    .setTypedConfig(httpConnectionManagerAny)
                    .build();
        } else {
            httpFilter = Filter.newBuilder()
                    .build();
        }
        FilterChain filterChain = FilterChain.newBuilder()
                .addFilters(httpFilter)
                .build();

        Address listenerAddress = Address.newBuilder()
                .setSocketAddress(SocketAddress.newBuilder()
                        .setAddress("127.0.0.1")
                        .setPortValue(8080)
                        .build())
                .build();

        return Listener.newBuilder()
                .setName(listenerName)
                .setAddress(listenerAddress)
                .addFilterChains(filterChain)
                .build();
    }

    private HttpConnectionManager createHttpConnectionManager(String routeName) {
        HttpConnectionManager.Builder managerBuilder = HttpConnectionManager.newBuilder();
        Rds.Builder rdsBuilder = Rds.newBuilder();
        rdsBuilder.setRouteConfigName(routeName);
        managerBuilder.setRds(rdsBuilder.build());
        return managerBuilder.build();
    }

    /**
     * Test when input listeners is null
     */
    @Test
    public void testResolveHttpFilterWithNullInput() {
        List<HttpFilter> result = LdsProtocolTransformer.resolveHttpFilter(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test when input listeners is empty list
     */
    @Test
    public void testResolveHttpFilterWithEmptyList() {
        List<HttpFilter> result = LdsProtocolTransformer.resolveHttpFilter(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test when no listener has name VIRTUAL_INBOUND
     */
    @Test
    public void testResolveHttpFilterWithNoVirtualInboundListener() {
        List<Listener> listeners = new ArrayList<>();
        when(nonVirtualListener.getName()).thenReturn(OTHER_LISTENER_NAME);
        listeners.add(nonVirtualListener);

        List<HttpFilter> result = LdsProtocolTransformer.resolveHttpFilter(listeners);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test when virtual inbound listener has no filter chains
     */
    @Test
    public void testResolveHttpFilterWithNoFilterChains() {
        List<Listener> listeners = new ArrayList<>();
        when(listener.getName()).thenReturn(VIRTUAL_INBOUND);
        when(listener.getFilterChainsList()).thenReturn(Collections.emptyList());
        listeners.add(listener);

        List<HttpFilter> result = LdsProtocolTransformer.resolveHttpFilter(listeners);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test when filter chain has no filters
     */
    @Test
    public void testResolveHttpFilterWithNoFilters() {
        List<Listener> listeners = new ArrayList<>();
        when(listener.getName()).thenReturn(VIRTUAL_INBOUND);
        when(listener.getFilterChainsList()).thenReturn(Collections.singletonList(filterChain));
        when(filterChain.getFiltersList()).thenReturn(Collections.emptyList());
        listeners.add(listener);

        List<HttpFilter> result = LdsProtocolTransformer.resolveHttpFilter(listeners);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test when filters don't contain HTTP_CONNECTION_MANAGER
     */
    @Test
    public void testResolveHttpFilterWithNoHttpConnectionManager() {
        List<Listener> listeners = new ArrayList<>();
        when(listener.getName()).thenReturn(VIRTUAL_INBOUND);
        when(listener.getFilterChainsList()).thenReturn(Collections.singletonList(filterChain));
        when(filterChain.getFiltersList()).thenReturn(Collections.singletonList(nonHttpFilter));
        when(nonHttpFilter.getName()).thenReturn("other_filter");
        listeners.add(listener);

        List<HttpFilter> result = LdsProtocolTransformer.resolveHttpFilter(listeners);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test the case where the input is null
     */
    @Test
    public void testResolveJwtWithNullInput() {
        Map<String, XdsJwtRule> result = LdsProtocolTransformer.resolveJwt(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test the case where the input is an empty list
     */
    @Test
    public void testResolveJwtWithEmptyList() {
        Map<String, XdsJwtRule> result = LdsProtocolTransformer.resolveJwt(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test cases where the JWT filter is not included in the list
     */
    @Test
    public void testResolveJwtWithoutJwtFilter() {
        when(otherFilter.getName()).thenReturn(OTHER_FILTER);
        List<HttpFilter> filters = Collections.singletonList(otherFilter);

        Map<String, XdsJwtRule> result = LdsProtocolTransformer.resolveJwt(filters);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test the case where the JWT filter is included but the configuration resolution fails
     */
    @Test
    public void testResolveJwtWithInvalidConfig() throws InvalidProtocolBufferException {
        when(jwtFilter.getName()).thenReturn(LDS_JWT_FILTER);
        when(jwtFilter.getTypedConfig()).thenReturn(typedConfig);
        List<HttpFilter> filters = Collections.singletonList(jwtFilter);
        Map<String, XdsJwtRule> result = LdsProtocolTransformer.resolveJwt(filters);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
