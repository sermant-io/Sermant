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

package io.sermant.router.spring.utils;

import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsRouteService;
import io.sermant.core.service.xds.XdsServiceDiscovery;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsClusterLoadAssigment;
import io.sermant.core.service.xds.entity.XdsHeaderMatcher;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.service.xds.entity.XdsPathMatcher;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.service.xds.entity.XdsRouteAction;
import io.sermant.core.service.xds.entity.XdsRouteMatch;
import io.sermant.core.service.xds.entity.match.ExactMatchStrategy;
import io.sermant.router.common.metric.MetricsManager;
import io.sermant.router.common.xds.lb.XdsLoadBalancer;
import io.sermant.router.common.xds.lb.XdsLoadBalancerFactory;
import io.sermant.router.common.xds.lb.XdsRoundRobinLoadBalancer;
import io.sermant.router.spring.TestServiceInstance;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * BaseHttpRouterUtilsTest
 *
 * @author daizhenyu
 * @since 2024-09-10
 **/
public class BaseHttpRouterUtilsTest {
    private static final String CLUSTER_NAME = "outbound|8080||serviceA.default.svc.cluster.local";

    private static final String SERVICE_NAME = "serviceA";

    private static MockedStatic<ServiceManager> serviceManager;

    private static MockedStatic<MetricsManager> metricsManager;

    private static MockedStatic<XdsLoadBalancerFactory> xdsLoadBalancerFactory;

    private static XdsServiceDiscovery serviceDiscovery;

    @BeforeClass
    public static void setUp() {
        XdsRouteService routeService = Mockito.mock(XdsRouteService.class);
        Mockito.when(routeService.isLocalityRoute(CLUSTER_NAME)).thenReturn(false);
        Mockito.when(routeService.getServiceRoute("serviceA")).thenReturn(createXdsRoute());
        serviceDiscovery = Mockito.mock(XdsServiceDiscovery.class);
        Mockito.when(serviceDiscovery.getClusterServiceInstance(CLUSTER_NAME))
                .thenReturn(Optional.of(createXdsClusterInstance(CLUSTER_NAME,
                        Arrays.asList("test-region-1"))));
        XdsCoreService xdsCoreService = Mockito.mock(XdsCoreService.class);
        Mockito.when(xdsCoreService.getXdsRouteService()).thenReturn(routeService);
        Mockito.when(xdsCoreService.getXdsServiceDiscovery()).thenReturn(serviceDiscovery);
        serviceManager = Mockito.mockStatic(ServiceManager.class);
        Mockito.when(ServiceManager.getService(XdsCoreService.class)).thenReturn(xdsCoreService);
        metricsManager = Mockito.mockStatic(MetricsManager.class);
        xdsLoadBalancerFactory = Mockito.mockStatic(XdsLoadBalancerFactory.class);
    }

    @AfterClass
    public static void tearDown() {
        serviceManager.close();
        metricsManager.close();
        xdsLoadBalancerFactory.close();
    }

    @Test
    public void testRebuildUrlByXdsServiceInstance() throws MalformedURLException, URISyntaxException {
        // prepare data
        TestServiceInstance testServiceInstance = new TestServiceInstance();
        testServiceInstance.setHost("127.0.0.1");
        testServiceInstance.setPort(8080);

        // use URI
        URI oldUri = new URI("http://example.com/test?param=value");
        Assert.assertEquals("http://127.0.0.1:8080/test?param=value",
                BaseHttpRouterUtils.rebuildUrlByXdsServiceInstance(oldUri, testServiceInstance));
    }

    @Test
    public void testChooseServiceInstanceByXds() {
        XdsLoadBalancer loadBalancer = new XdsRoundRobinLoadBalancer();
        Mockito.when(XdsLoadBalancerFactory.getLoadBalancer(Mockito.any(), Mockito.any())).thenReturn(loadBalancer);

        Map<String, String> headers = new HashMap<>();
        headers.put("version", "v1");

        // service instance is empty
        Mockito.when(serviceDiscovery.getClusterServiceInstance(SERVICE_NAME, CLUSTER_NAME))
                .thenReturn(Optional.of(createXdsClusterInstance(CLUSTER_NAME, new ArrayList<>())));
        Optional<ServiceInstance> result = BaseHttpRouterUtils
                .chooseServiceInstanceByXds("serviceA", "/test", headers);
        Assert.assertFalse(result.isPresent());

        // route not match and service instance is not empty
        result = BaseHttpRouterUtils
                .chooseServiceInstanceByXds("serviceA", "/test-invalid", Collections.emptyMap());
        Assert.assertFalse(result.isPresent());

        // route match and service instance is not empty
        Mockito.when(serviceDiscovery.getClusterServiceInstance(SERVICE_NAME, CLUSTER_NAME))
                .thenReturn(Optional.of(createXdsClusterInstance(CLUSTER_NAME, Arrays.asList("region-1"))));
        result = BaseHttpRouterUtils
                .chooseServiceInstanceByXds("serviceA", "/test", headers);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("serviceA", result.get().getServiceName());
    }

    @Test
    public void testIsXdsRouteRequired() {
        // empty string
        Assert.assertFalse(BaseHttpRouterUtils.isXdsRouteRequired(""));

        // null
        Assert.assertFalse(BaseHttpRouterUtils.isXdsRouteRequired(null));

        // start with number
        Assert.assertFalse(BaseHttpRouterUtils.isXdsRouteRequired("192"));

        // host
        Assert.assertFalse(BaseHttpRouterUtils.isXdsRouteRequired("localhost"));
    }

    @Test
    public void testProcessHeaders() {
        // every header has value
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Header1", Arrays.asList("Value1", "Value2"));
        headers.put("Header2", Collections.singletonList("Value3"));

        Map<String, String> result = BaseHttpRouterUtils.processHeaders(headers);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Value1", result.get("Header1"));
        Assert.assertEquals("Value3", result.get("Header2"));

        // header has empty value
        headers = new HashMap<>();
        headers.put("Header1", Arrays.asList("Value1", "Value2"));
        headers.put("Header2", Collections.emptyList());

        result = BaseHttpRouterUtils.processHeaders(headers);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Value1", result.get("Header1"));
        Assert.assertNull(result.get("Header2"));

        // header has null value
        headers = new HashMap<>();
        headers.put("Header1", Arrays.asList("Value1", "Value2"));
        headers.put("Header2", null);

        result = BaseHttpRouterUtils.processHeaders(headers);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Value1", result.get("Header1"));
        Assert.assertNull(result.get("Header2"));
    }

    private static List<XdsRoute> createXdsRoute() {
        XdsRoute route = new XdsRoute();
        route.setName("test-route");

        XdsRouteMatch xdsRouteMatch = new XdsRouteMatch();
        XdsPathMatcher pathMatcher = new XdsPathMatcher(new ExactMatchStrategy("/test"), true);
        XdsHeaderMatcher headerMatcher = new XdsHeaderMatcher("version", new ExactMatchStrategy("v1"));
        xdsRouteMatch.setCaseSensitive(true);
        xdsRouteMatch.setPathMatcher(pathMatcher);
        xdsRouteMatch.setHeaderMatchers(Arrays.asList(headerMatcher));

        XdsRouteAction xdsRouteAction = new XdsRouteAction();
        xdsRouteAction.setCluster("outbound|8080||serviceA.default.svc.cluster.local");

        route.setRouteMatch(xdsRouteMatch);
        route.setRouteAction(xdsRouteAction);
        return Arrays.asList(route);
    }

    private static XdsClusterLoadAssigment createXdsClusterInstance(String clusterName, List<String> localityList) {
        Map<XdsLocality, Set<ServiceInstance>> localityInstances = new HashMap<>();
        for (String region : localityList) {
            Set<ServiceInstance> instances = new HashSet<>();
            TestServiceInstance testServiceInstance = new TestServiceInstance();
            testServiceInstance.setService("serviceA");
            Map<String, String> metaData = new HashMap<>();
            metaData.put("region", region);
            testServiceInstance.setMetaData(metaData);
            instances.add(testServiceInstance);
            XdsLocality locality = new XdsLocality();
            locality.setRegion(region);
            localityInstances.put(locality, instances);
        }

        XdsClusterLoadAssigment clusterInstance = new XdsClusterLoadAssigment();
        clusterInstance.setClusterName(clusterName);
        clusterInstance.setLocalityInstances(localityInstances);

        return clusterInstance;
    }
}
