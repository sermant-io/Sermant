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

package io.sermant.flowcontrol.common.util;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsFlowControlService;
import io.sermant.core.service.xds.XdsLoadBalanceService;
import io.sermant.core.service.xds.XdsRouteService;
import io.sermant.core.service.xds.XdsServiceDiscovery;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsDelay;
import io.sermant.core.service.xds.entity.XdsHeaderOption;
import io.sermant.core.service.xds.entity.XdsHttpFault;
import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.core.service.xds.entity.XdsPathMatcher;
import io.sermant.core.service.xds.entity.XdsRateLimit;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRetryPolicy;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.service.xds.entity.XdsRouteAction;
import io.sermant.core.service.xds.entity.XdsRouteMatch;
import io.sermant.core.service.xds.entity.match.ExactMatchStrategy;
import io.sermant.core.service.xds.entity.match.MatchStrategy;
import io.sermant.core.utils.NetworkUtils;
import io.sermant.implement.service.xds.entity.XdsServiceInstance;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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
 * XdsAbstractTest
 *
 * @author zhp
 * @since 2024-12-10
 **/
public abstract class XdsAbstractTest {
    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private static MockedStatic<NetworkUtils> networkUtils;

    private static MockedStatic<ConfigManager> configManager;

    protected static final String SERVICE_NAME = "provider";

    protected static final String ROUTE_NAME = "routeA";

    protected static final String PATH = "/test";

    protected static final String CLUSTER_NAME = "outbound|8080||serviceA.default.svc.cluster.local";

    protected final XdsRequestCircuitBreakers requestCircuitBreakers = new XdsRequestCircuitBreakers();

    protected final XdsInstanceCircuitBreakers instanceCircuitBreakers = new XdsInstanceCircuitBreakers();

    protected final XdsRetryPolicy retryPolicy = new XdsRetryPolicy();

    protected final XdsRateLimit rateLimit = new XdsRateLimit();

    protected final XdsHttpFault httpFault = new XdsHttpFault();

    @Before
    public void setUp() {
        networkUtils = Mockito.mockStatic(NetworkUtils.class);
        Mockito.when(NetworkUtils.getKubernetesPodIp()).thenReturn("127.0.0.1");
        configManager = Mockito.mockStatic(ConfigManager.class);
        List<XdsRoute> xdsRouteList = new ArrayList<>();
        XdsRoute xdsRoute = new XdsRoute();
        xdsRoute.setName(ROUTE_NAME);
        XdsRouteMatch xdsRouteMatch = new XdsRouteMatch();
        MatchStrategy matchStrategy = new ExactMatchStrategy(PATH);
        XdsPathMatcher xdsPathMatcher = new XdsPathMatcher(matchStrategy, false);
        xdsRouteMatch.setPathMatcher(xdsPathMatcher);
        xdsRouteMatch.setHeaderMatchers(new ArrayList<>());
        xdsRoute.setRouteMatch(xdsRouteMatch);
        XdsRouteAction xdsRouteAction = new XdsRouteAction();
        xdsRouteAction.setWeighted(true);
        XdsRouteAction.XdsClusterWeight xdsClusterWeight = new XdsRouteAction.XdsClusterWeight();
        xdsClusterWeight.setClusterName(CLUSTER_NAME);
        xdsClusterWeight.setWeight(100);
        XdsRouteAction.XdsWeightedClusters xdsWeightedClusters = new XdsRouteAction.XdsWeightedClusters();
        xdsWeightedClusters.setClusters(Collections.singletonList(xdsClusterWeight));
        xdsWeightedClusters.setTotalWeight(100);
        xdsRouteAction.setWeightedClusters(xdsWeightedClusters);
        xdsRoute.setRouteAction(xdsRouteAction);
        xdsRouteList.add(xdsRoute);
        XdsCoreService xdsCoreService = Mockito.mock(XdsCoreService.class);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(()->ServiceManager.getService(XdsCoreService.class)).thenReturn(xdsCoreService);
        XdsRouteService xdsRouteService = Mockito.mock(XdsRouteService.class);
        XdsServiceDiscovery serviceDiscovery = Mockito.mock(XdsServiceDiscovery.class);
        XdsFlowControlService xdsFlowControlService = Mockito.mock(XdsFlowControlService.class);
        XdsLoadBalanceService xdsLoadBalanceService = Mockito.mock(XdsLoadBalanceService.class);
        Mockito.when(xdsCoreService.getXdsRouteService()).thenReturn(xdsRouteService);
        Mockito.when(xdsCoreService.getXdsServiceDiscovery()).thenReturn(serviceDiscovery);
        Mockito.when(xdsCoreService.getXdsFlowControlService()).thenReturn(xdsFlowControlService);
        Mockito.when(xdsCoreService.getLoadBalanceService()).thenReturn(xdsLoadBalanceService);
        requestCircuitBreakers.setMaxRequests(1000);
        instanceCircuitBreakers.setInterval(1000);
        retryPolicy.setRetryOn("503");
        rateLimit.setResponseHeaderOption(Collections.singletonList(new XdsHeaderOption()));
        XdsDelay delay = new XdsDelay();
        delay.setFixedDelay(1000);
        httpFault.setDelay(delay);
        Mockito.when(xdsRouteService.getServiceRoute(SERVICE_NAME)).thenReturn(xdsRouteList);
        Mockito.when(xdsFlowControlService.getRequestCircuitBreakers(SERVICE_NAME, CLUSTER_NAME))
                .thenReturn(Optional.of(requestCircuitBreakers));
        Mockito.when(xdsFlowControlService.getInstanceCircuitBreakers(SERVICE_NAME, CLUSTER_NAME))
                .thenReturn(Optional.of(instanceCircuitBreakers));
        Mockito.when(xdsFlowControlService.getRetryPolicy(SERVICE_NAME, ROUTE_NAME))
                .thenReturn(Optional.of(retryPolicy));
        Mockito.when(xdsFlowControlService.getRateLimit(SERVICE_NAME, ROUTE_NAME, "8080"))
                .thenReturn(Optional.of(rateLimit));
        Mockito.when(xdsFlowControlService.getHttpFault(SERVICE_NAME, ROUTE_NAME))
                .thenReturn(Optional.of(httpFault));
        Mockito.when(serviceDiscovery.getServiceInstance(SERVICE_NAME))
                .thenReturn(createServiceInstance4Service(Arrays.asList("127.0.0.1", "host", "localhost")));
        Mockito.when(xdsLoadBalanceService.getLbPolicyOfCluster(SERVICE_NAME, CLUSTER_NAME))
                .thenReturn(XdsLbPolicy.RANDOM);
    }

    @After
    public void tearDown() {
        serviceManagerMockedStatic.close();
        networkUtils.close();
        configManager.close();
    }

    private static Set<ServiceInstance> createServiceInstance4Service(List<String> hosts) {
        Set<ServiceInstance> serviceInstances = new HashSet<>();
        for (String host : hosts) {
            XdsServiceInstance serviceInstance = new XdsServiceInstance();
            serviceInstance.setHost(host);
            Map<String, String> metaData = new HashMap<>();
            metaData.put("region", host);
            serviceInstance.setMetadata(metaData);
            serviceInstances.add(serviceInstance);
        }
        return serviceInstances;
    }
}
