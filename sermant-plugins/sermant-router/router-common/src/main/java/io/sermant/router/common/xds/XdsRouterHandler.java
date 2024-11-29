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

import io.sermant.core.common.LoggerFactory;
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
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.metric.MetricsManager;
import io.sermant.router.common.utils.XdsRouterUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * XdsRouterHandler, filter service instances based on xDS routing rules
 *
 * @author daizhenyu
 * @since 2024-08-29
 **/
public enum XdsRouterHandler {
    /**
     * singleton
     */
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Random random = new Random();

    private XdsRouteService routeService;

    private XdsServiceDiscovery serviceDiscovery;

    /**
     * constructor
     */
    XdsRouterHandler() {
        XdsCoreService xdsCoreService = ServiceManager.getService(XdsCoreService.class);
        if (xdsCoreService != null) {
            routeService = xdsCoreService.getXdsRouteService();
            serviceDiscovery = xdsCoreService.getXdsServiceDiscovery();
        }
    }

    /**
     * getServiceInstanceByXdsRoute
     *
     * @param serviceName service name
     * @param path request path
     * @return serviceInstance
     */
    public Set<ServiceInstance> getServiceInstanceByXdsRoute(String serviceName, String path) {
        return getMatchedServiceInstance(serviceName, path, null, MatchType.PATH);
    }

    /**
     * getServiceInstanceByXdsRoute
     *
     * @param serviceName service name
     * @param headers request headers
     * @return serviceInstance
     */
    public Set<ServiceInstance> getServiceInstanceByXdsRoute(String serviceName, Map<String, String> headers) {
        return getMatchedServiceInstance(serviceName, null, headers, MatchType.HEADER);
    }

    /**
     * getServiceInstanceByXdsRoute
     *
     * @param serviceName service name
     * @param path request path
     * @param headers request headers
     * @return serviceInstance
     */
    public Set<ServiceInstance> getServiceInstanceByXdsRoute(String serviceName, String path,
            Map<String, String> headers) {
        return getMatchedServiceInstance(serviceName, path, headers, MatchType.BOTH);
    }

    private Set<ServiceInstance> getMatchedServiceInstance(String serviceName, String path,
            Map<String, String> headers, MatchType matchType) {
        if (routeService == null || serviceDiscovery == null) {
            LOGGER.severe("xDS service not open for xDS routing.");
            return Collections.EMPTY_SET;
        }
        List<XdsRoute> routes = routeService.getServiceRoute(serviceName);
        XdsRoute matchedRoute = null;
        boolean pathMatched = matchType == MatchType.PATH || matchType == MatchType.BOTH;
        boolean headerMatched = matchType == MatchType.HEADER || matchType == MatchType.BOTH;
        for (XdsRoute route : routes) {
            XdsRouteMatch routeMatch = route.getRouteMatch();

            // check path matching
            if (pathMatched && !isPathMatched(routeMatch.getPathMatcher(), path)) {
                continue;
            }

            // check head matching
            if (headerMatched && !isHeadersMatched(routeMatch.getHeaderMatchers(), headers)) {
                continue;
            }
            matchedRoute = route;
            break;
        }

        if (matchedRoute == null) {
            return serviceDiscovery.getServiceInstance(serviceName);
        }
        return handleXdsRoute(matchedRoute, serviceName);
    }

    private Set<ServiceInstance> handleXdsRoute(XdsRoute route, String serviceName) {
        // select cluster
        XdsRouteAction routeAction = route.getRouteAction();
        String cluster = routeAction.getCluster();
        if (routeAction.isWeighted()) {
            cluster = selectClusterByWeight(routeAction.getWeightedClusters());
        }
        MetricsManager.collectXdsRouterDestinationTagCountMetric(cluster);

        // get service instance of cluster
        Optional<XdsClusterLoadAssigment> loadAssigmentOptional =
                serviceDiscovery.getClusterServiceInstance(serviceName, cluster);
        if (!loadAssigmentOptional.isPresent()) {
            return serviceDiscovery.getServiceInstance(serviceName);
        }
        XdsClusterLoadAssigment clusterLoadAssigment = loadAssigmentOptional.get();

        if (!routeService.isLocalityRoute(serviceName, clusterLoadAssigment.getClusterName())) {
            Set<ServiceInstance> serviceInstances = getServiceInstanceOfCluster(clusterLoadAssigment);
            return serviceInstances.isEmpty() ? serviceDiscovery.getServiceInstance(serviceName) : serviceInstances;
        }

        // get locality info of self-service and route by locality
        Optional<XdsLocality> localityInfoOfSelfService = XdsRouterUtils.getLocalityInfoOfSelfService();
        if (localityInfoOfSelfService.isPresent()) {
            Set<ServiceInstance> serviceInstances = getServiceInstanceOfLocalityCluster(clusterLoadAssigment,
                    localityInfoOfSelfService.get());
            if (!serviceInstances.isEmpty()) {
                return serviceInstances;
            }
        }

        Set<ServiceInstance> serviceInstances = getServiceInstanceOfCluster(clusterLoadAssigment);
        return serviceInstances.isEmpty() ? serviceDiscovery.getServiceInstance(serviceName) : serviceInstances;
    }

    private Set<ServiceInstance> getServiceInstanceOfLocalityCluster(XdsClusterLoadAssigment clusterLoadAssigment,
            XdsLocality locality) {
        return clusterLoadAssigment.getLocalityInstances().entrySet().stream()
                .filter(xdsLocalitySetEntry -> isSameLocality(locality, xdsLocalitySetEntry.getKey()))
                .flatMap(xdsLocalitySetEntry -> xdsLocalitySetEntry.getValue().stream())
                .collect(Collectors.toSet());
    }

    private boolean isSameLocality(XdsLocality selfLocality, XdsLocality serviceLocality) {
        if (!selfLocality.getRegion().equals(serviceLocality.getRegion())) {
            return false;
        }
        if (StringUtils.isEmpty(selfLocality.getZone())) {
            return true;
        }
        if (!selfLocality.getZone().equals(serviceLocality.getZone())) {
            return false;
        }
        if (StringUtils.isEmpty(selfLocality.getSubZone())) {
            return true;
        }
        return selfLocality.getSubZone().equals(serviceLocality.getSubZone());
    }

    private Set<ServiceInstance> getServiceInstanceOfCluster(XdsClusterLoadAssigment clusterLoadAssigment) {
        Set<ServiceInstance> serviceInstances = new HashSet<>();
        for (Entry<XdsLocality, Set<ServiceInstance>> xdsLocalitySetEntry : clusterLoadAssigment.getLocalityInstances()
                .entrySet()) {
            serviceInstances.addAll(xdsLocalitySetEntry.getValue());
        }
        return serviceInstances;
    }

    private boolean isPathMatched(XdsPathMatcher matcher, String path) {
        return matcher.isMatch(path);
    }

    private boolean isHeadersMatched(List<XdsHeaderMatcher> matchers, Map<String, String> headers) {
        for (XdsHeaderMatcher matcher : matchers) {
            if (!matcher.isMatch(headers)) {
                return false;
            }
        }
        return true;
    }

    private String selectClusterByWeight(XdsWeightedClusters weightedClusters) {
        List<XdsClusterWeight> clusters = weightedClusters.getClusters();
        int totalWeight = weightedClusters.getTotalWeight();
        if (CollectionUtils.isEmpty(clusters) || totalWeight == 0) {
            return StringUtils.EMPTY;
        }
        int randomWeight = random.nextInt(totalWeight);

        int currentWeight = 0;
        for (XdsClusterWeight clusterWeight : clusters) {
            currentWeight += clusterWeight.getWeight();
            if (randomWeight < currentWeight) {
                return clusterWeight.getClusterName();
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * route MatchType
     *
     * @author daizhenyu
     * @since 2024-08-29
     **/
    private enum MatchType {
        /**
         * path match
         */
        PATH,
        /**
         * header match
         */
        HEADER,
        /**
         * path and header match
         */
        BOTH
    }
}
