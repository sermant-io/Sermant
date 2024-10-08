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

import com.google.protobuf.Any;

import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.Rds;
import io.sermant.core.service.xds.entity.XdsHttpConnectionManager;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LdsProtocolTransformerTest
 *
 * @author daizhenyu
 * @since 2024-08-23
 **/
public class LdsProtocolTransformerTest {
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
        Assert.assertEquals(2, httpConnectionManagers.size());
        Set<String> actualRouteConfigNames = new HashSet<>();
        actualRouteConfigNames.add(httpConnectionManagers.get(0).getRouteConfigName());
        actualRouteConfigNames.add(httpConnectionManagers.get(1).getRouteConfigName());

        Set<String> expectRouteConfigNames = new HashSet<>();
        expectRouteConfigNames.add("test-routeA");
        expectRouteConfigNames.add("test-routeB");
        Assert.assertEquals(expectRouteConfigNames, actualRouteConfigNames);
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
}
