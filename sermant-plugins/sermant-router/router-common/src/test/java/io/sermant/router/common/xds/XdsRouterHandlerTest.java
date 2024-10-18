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

package io.sermant.router.common.xds;

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
import io.sermant.core.service.xds.entity.XdsRouteAction.XdsClusterWeight;
import io.sermant.core.service.xds.entity.XdsRouteAction.XdsWeightedClusters;
import io.sermant.core.service.xds.entity.XdsRouteMatch;
import io.sermant.core.service.xds.entity.match.ExactMatchStrategy;
import io.sermant.router.common.utils.XdsRouterUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * XdsRouterHandlerTest
 *
 * @author daizhenyu
 * @since 2024-08-30
 **/
public class XdsRouterHandlerTest {
    private static final String CLUSTER_NAME = "outbound|8080||serviceA.default.svc.cluster.local";

    private static MockedStatic<ServiceManager> serviceManager;

    private static MockedStatic<XdsRouterUtils> xdsRouterUtil;

    private static XdsLocality locality1;

    private static XdsLocality locality3;

    @BeforeClass
    public static void setUp() {
        XdsRouteService routeService = Mockito.mock(XdsRouteService.class);
        XdsServiceDiscovery serviceDiscovery = Mockito.mock(XdsServiceDiscovery.class);
        XdsCoreService xdsCoreService = Mockito.mock(XdsCoreService.class);
        Mockito.when(xdsCoreService.getXdsRouteService()).thenReturn(routeService);
        Mockito.when(xdsCoreService.getXdsServiceDiscovery()).thenReturn(serviceDiscovery);
        Mockito.when(routeService.isLocalityRoute(CLUSTER_NAME)).thenReturn(true);
        Mockito.when(routeService.getServiceRoute("serviceA")).thenReturn(createXdsRoute());

        serviceManager = Mockito.mockStatic(ServiceManager.class);
        Mockito.when(ServiceManager.getService(XdsCoreService.class)).thenReturn(xdsCoreService);

        xdsRouterUtil = Mockito.mockStatic(XdsRouterUtils.class);
        XdsLocality locality = new XdsLocality();
        locality.setRegion("test-region-1");
        Mockito.when(XdsRouterUtils.getLocalityInfoOfSelfService()).thenReturn(Optional.of(locality));

        Mockito.when(serviceDiscovery.getServiceInstance("serviceA")).thenReturn(createServiceInstance4Service());
        Mockito.when(serviceDiscovery.getClusterServiceInstance(CLUSTER_NAME))
                .thenReturn(Optional.of(createXdsClusterInstance(CLUSTER_NAME,
                        Arrays.asList("test-region-1", "test-region-2"))));

        locality1 = new XdsLocality();
        locality1.setRegion("test-region-1");

        locality3 = new XdsLocality();
        locality3.setRegion("test-region-3");
    }

    @AfterClass
    public static void tearDown() {
        serviceManager.close();
        xdsRouterUtil.close();
    }

    @Test
    public void testGetServiceInstanceByXdsRouteWithPath() {
        Mockito.when(XdsRouterUtils.getLocalityInfoOfSelfService()).thenReturn(Optional.of(locality1));

        // path not match
        Set<ServiceInstance> instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", "/test1");
        Assert.assertEquals(4, instances.size());

        //path match and locality route
        instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", "/test");
        Assert.assertEquals(1, instances.size());

        //path match and not locality route
        Mockito.when(XdsRouterUtils.getLocalityInfoOfSelfService()).thenReturn(Optional.of(locality3));
        instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", "/test");
        Assert.assertEquals(2, instances.size());
    }

    @Test
    public void testGetServiceInstanceByXdsRouteWithHeader() {
        Mockito.when(XdsRouterUtils.getLocalityInfoOfSelfService()).thenReturn(Optional.of(locality1));
        Map<String, String> headers = new HashMap<>();
        headers.put("test", "test");

        // header not match
        Set<ServiceInstance> instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", headers);
        Assert.assertEquals(4, instances.size());

        //header match and locality route
        headers.put("version", "v1");
        instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", headers);
        Assert.assertEquals(1, instances.size());

        //header match and not locality route
        Mockito.when(XdsRouterUtils.getLocalityInfoOfSelfService()).thenReturn(Optional.of(locality3));
        instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", headers);
        Assert.assertEquals(2, instances.size());
    }

    @Test
    public void testGetServiceInstanceByXdsRouteWithPathAndHeader() {
        Mockito.when(XdsRouterUtils.getLocalityInfoOfSelfService()).thenReturn(Optional.of(locality1));

        Map<String, String> headers = new HashMap<>();
        headers.put("test", "test");
        // path and header all not match
        Set<ServiceInstance> instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", "/test1",
                headers);
        Assert.assertEquals(4, instances.size());

        // path match, header not match
        instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", "/test",
                headers);
        Assert.assertEquals(4, instances.size());

        // path not match, header match
        headers.put("version", "v1");
        instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", "/test1",
                headers);
        Assert.assertEquals(4, instances.size());

        //path and header all match and locality route
        instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", "/test", headers);
        Assert.assertEquals(1, instances.size());

        //path and header all match and not locality route
        Mockito.when(XdsRouterUtils.getLocalityInfoOfSelfService()).thenReturn(Optional.of(locality3));
        instances = XdsRouterHandler.INSTANCE.getServiceInstanceByXdsRoute("serviceA", "/test");
        Assert.assertEquals(2, instances.size());
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
        xdsRouteAction.setWeighted(true);
        XdsWeightedClusters weightedClusters = new XdsWeightedClusters();

        XdsClusterWeight clusterWeight = new XdsClusterWeight();
        clusterWeight.setClusterName("outbound|8080||serviceA.default.svc.cluster.local");
        clusterWeight.setWeight(100);

        weightedClusters.setTotalWeight(100);
        weightedClusters.setClusters(Arrays.asList(clusterWeight));
        xdsRouteAction.setWeightedClusters(weightedClusters);

        route.setRouteMatch(xdsRouteMatch);
        route.setRouteAction(xdsRouteAction);
        return Arrays.asList(route);
    }

    private static XdsClusterLoadAssigment createXdsClusterInstance(String clusterName, List<String> localityList) {
        Map<XdsLocality, Set<ServiceInstance>> localityInstances = new HashMap<>();
        for (String region : localityList) {
            Set<ServiceInstance> instances = new HashSet<>();
            TestServiceInstance testServiceInstance = new TestServiceInstance();
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

    private static Set<ServiceInstance> createServiceInstance4Service() {
        Set<ServiceInstance> serviceInstances = new HashSet<>();
        serviceInstances.add(new TestServiceInstance());
        serviceInstances.add(new TestServiceInstance());
        serviceInstances.add(new TestServiceInstance());
        serviceInstances.add(new TestServiceInstance());
        return serviceInstances;
    }
}