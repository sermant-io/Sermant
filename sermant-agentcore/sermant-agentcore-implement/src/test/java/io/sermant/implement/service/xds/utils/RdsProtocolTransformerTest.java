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

import com.google.protobuf.BoolValue;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.Route.Builder;
import io.envoyproxy.envoy.config.route.v3.RouteAction;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.config.route.v3.WeightedCluster;
import io.envoyproxy.envoy.config.route.v3.WeightedCluster.ClusterWeight;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.service.xds.entity.XdsRouteConfiguration;
import io.sermant.core.service.xds.entity.XdsVirtualHost;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RdsProtocolTransformerTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class RdsProtocolTransformerTest {
    private List<RouteConfiguration> routeConfigurations;

    @Test
    public void testGetRouteConfigurationsWithPathTypeAndHeaderTypeAllExact() {
        routeConfigurations = Arrays.asList(
                createRouteConfiguration("exact", "exact")
        );
        List<XdsRouteConfiguration> result = RdsProtocolTransformer
                .getRouteConfigurations(routeConfigurations);
        Assert.assertEquals(1, result.size());

        // pathType:exact headerType:exact
        XdsRouteConfiguration routeConfiguration = result.get(0);
        Assert.assertEquals("testRoute", routeConfiguration.getRouteConfigName());
        Map<String, XdsVirtualHost> virtualHosts = routeConfiguration.getVirtualHosts();
        Assert.assertEquals(1, virtualHosts.size());
        Assert.assertTrue(virtualHosts.containsKey("serviceA"));
        XdsVirtualHost virtualHost = virtualHosts.get("serviceA");
        List<XdsRoute> routes = virtualHost.getRoutes();
        Assert.assertEquals(2, routes.size());
        Set<Boolean> expectIsWeightedCluster = new HashSet<>();
        expectIsWeightedCluster.add(true);
        expectIsWeightedCluster.add(false);
        Set<Boolean> actualIsWeightedCluster = new HashSet<>();
        actualIsWeightedCluster.add(routes.get(0).getRouteAction().isWeighted());
        actualIsWeightedCluster.add(routes.get(1).getRouteAction().isWeighted());
        Assert.assertEquals(expectIsWeightedCluster, actualIsWeightedCluster);
        Assert.assertTrue(routes.get(0).getRouteMatch().getPathMatcher().isMatch("/test"));
        Map<String, String> headers = new HashMap<>();
        headers.put("testHeader", "value");
        Assert.assertTrue(routes.get(0).getRouteMatch().getHeaderMatchers().get(0).isMatch(headers));
    }

    @Test
    public void testGetRouteConfigurationsWithPathTypeAndHeaderTypeAllPrefix() {
        List<RouteConfiguration> routeConfigurations = Arrays.asList(
                createRouteConfiguration("prefix", "prefix")
        );
        List<XdsRouteConfiguration> result = RdsProtocolTransformer
                .getRouteConfigurations(routeConfigurations);
        Assert.assertEquals(1, result.size());

        // pathType:prefix headerType:prefix
        XdsRouteConfiguration routeConfiguration = result.get(0);
        Assert.assertEquals("testRoute", routeConfiguration.getRouteConfigName());
        Map<String, XdsVirtualHost> virtualHosts = routeConfiguration.getVirtualHosts();
        Assert.assertEquals(1, virtualHosts.size());
        Assert.assertTrue(virtualHosts.containsKey("serviceA"));
        XdsVirtualHost virtualHost = virtualHosts.get("serviceA");
        List<XdsRoute> routes = virtualHost.getRoutes();
        Assert.assertEquals(2, routes.size());
        Set<Boolean> expectIsWeightedCluster = new HashSet<>();
        expectIsWeightedCluster.add(true);
        expectIsWeightedCluster.add(false);
        Set<Boolean> actualIsWeightedCluster = new HashSet<>();
        actualIsWeightedCluster.add(routes.get(0).getRouteAction().isWeighted());
        actualIsWeightedCluster.add(routes.get(1).getRouteAction().isWeighted());
        Assert.assertEquals(expectIsWeightedCluster, actualIsWeightedCluster);
        Assert.assertTrue(routes.get(0).getRouteMatch().getPathMatcher().isMatch("/test/2"));
        Map<String, String> headers = new HashMap<>();
        headers.put("testHeader", "value-prefix");
        Assert.assertTrue(routes.get(0).getRouteMatch().getHeaderMatchers().get(0).isMatch(headers));
    }

    @Test
    public void testGetRouteConfigurationsWithPathTypePrefixAndHeaderTypePresent() {
        List<RouteConfiguration> routeConfigurations = Arrays.asList(
                createRouteConfiguration("prefix", "present")
        );
        List<XdsRouteConfiguration> result = RdsProtocolTransformer
                .getRouteConfigurations(routeConfigurations);
        Assert.assertEquals(1, result.size());

        // pathType:prefix headerType:present
        XdsRouteConfiguration routeConfiguration = result.get(0);
        Assert.assertEquals("testRoute", routeConfiguration.getRouteConfigName());
        Map<String, XdsVirtualHost> virtualHosts = routeConfiguration.getVirtualHosts();
        Assert.assertEquals(1, virtualHosts.size());
        Assert.assertTrue(virtualHosts.containsKey("serviceA"));
        XdsVirtualHost virtualHost = virtualHosts.get("serviceA");
        List<XdsRoute> routes = virtualHost.getRoutes();
        Assert.assertEquals(2, routes.size());
        Set<Boolean> expectIsWeightedCluster = new HashSet<>();
        expectIsWeightedCluster.add(true);
        expectIsWeightedCluster.add(false);
        Set<Boolean> actualIsWeightedCluster = new HashSet<>();
        actualIsWeightedCluster.add(routes.get(0).getRouteAction().isWeighted());
        actualIsWeightedCluster.add(routes.get(1).getRouteAction().isWeighted());
        Assert.assertEquals(expectIsWeightedCluster, actualIsWeightedCluster);
        Assert.assertTrue(routes.get(0).getRouteMatch().getPathMatcher().isMatch("/test/3"));
        Map<String, String> headers = new HashMap<>();
        headers.put("testHeader", "present");
        Assert.assertTrue(routes.get(0).getRouteMatch().getHeaderMatchers().get(0).isMatch(headers));
    }

    private RouteConfiguration createRouteConfiguration(String pathType, String headerType) {
        return RouteConfiguration.newBuilder()
                .setName("testRoute")
                .addVirtualHosts(createVirtualHost(pathType, headerType))
                .build();
    }

    private VirtualHost createVirtualHost(String pathType, String headerType) {
        return VirtualHost.newBuilder()
                .setName("serviceA.example.com")
                .addDomains("serviceA.example.com")
                .addRoutes(createRoute(true, pathType, headerType))
                .addRoutes(createRoute(false, pathType, headerType))
                .build();
    }

    private Route createRoute(boolean isWeightCluster, String pathType, String headerType) {
        Builder routeBuilder = Route.newBuilder();
        if (isWeightCluster) {
            routeBuilder.setRoute(createRouteActionWithWeightCluster());
        } else {
            routeBuilder.setRoute(createRouteAction());
        }
        return routeBuilder.setMatch(createRouteMatch(pathType, headerType))
                .build();
    }

    private RouteMatch createRouteMatch(String pathType, String headerType) {
        RouteMatch.Builder matchBuilder = RouteMatch.newBuilder();
        if ("exact".equals(pathType)) {
            matchBuilder.setPath("/test");
        } else if ("prefix".equals(pathType)) {
            matchBuilder.setPrefix("/test");
        }
        return matchBuilder
                .setCaseSensitive(BoolValue.of(true))
                .addHeaders(createHeaderMatcher(headerType))
                .build();
    }

    private RouteAction createRouteAction() {
        return RouteAction.newBuilder()
                .setCluster("testCluster")
                .build();
    }

    private RouteAction createRouteActionWithWeightCluster() {
        return RouteAction.newBuilder()
                .setWeightedClusters(createWeightedCluster())
                .build();
    }

    private HeaderMatcher createHeaderMatcher(String headerType) {
        HeaderMatcher.Builder headBuilder = HeaderMatcher.newBuilder().setName("testHeader");
        StringMatcher.Builder stringMatcherBuilder = StringMatcher.newBuilder();
        switch (headerType) {
            case "exact":
                headBuilder.setStringMatch(stringMatcherBuilder.setExact("value"));
                break;
            case "prefix":
                headBuilder.setStringMatch(stringMatcherBuilder.setPrefix("value"));
                break;
            case "present":
                headBuilder.setPresentMatch(true);
                break;
        }
        return headBuilder.build();
    }

    private WeightedCluster createWeightedCluster() {
        return WeightedCluster.newBuilder()
                .setTotalWeight(UInt32Value.of(100))
                .addClusters(createClusterWeight())
                .build();
    }

    private ClusterWeight createClusterWeight() {
        return ClusterWeight.newBuilder()
                .setName("clusterName")
                .setWeight(UInt32Value.of(100))
                .build();
    }
}
