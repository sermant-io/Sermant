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

package io.sermant.flowcontrol.common.xds.handler;

import io.sermant.core.common.CommonConstant;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsFlowControlService;
import io.sermant.core.service.xds.XdsLoadBalanceService;
import io.sermant.core.service.xds.XdsRouteService;
import io.sermant.core.service.xds.XdsServiceDiscovery;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsClusterLoadAssigment;
import io.sermant.core.service.xds.entity.XdsHttpFault;
import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.service.xds.entity.XdsRateLimit;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRetryPolicy;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.utils.StringUtils;
import io.sermant.flowcontrol.common.util.XdsRouterUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Xds handler
 *
 * @author zhp
 * @since 2024-11-28
 */
public enum XdsHandler {
    /**
     * singleton
     */
    INSTANCE;

    /**
     * Length of the cluster name after splitting
     */
    private static final int CLUSTER_NAME_SPLIT_LENGTH = 4;

    private XdsFlowControlService xdsFlowControlService;

    private XdsRouteService xdsRouteService;

    private XdsServiceDiscovery xdsServiceDiscovery;

    private XdsLoadBalanceService xdsLoadBalanceService;

    /**
     * constructor
     */
    XdsHandler() {
        Logger logger = LoggerFactory.getLogger();
        try {
            XdsCoreService xdsCoreService = ServiceManager.getService(XdsCoreService.class);
            xdsRouteService = xdsCoreService.getXdsRouteService();
            xdsServiceDiscovery = xdsCoreService.getXdsServiceDiscovery();
            xdsFlowControlService = xdsCoreService.getXdsFlowControlService();
            xdsLoadBalanceService = xdsCoreService.getLoadBalanceService();
        } catch (IllegalArgumentException e) {
            logger.severe("XdsCoreService not started");
        }
    }

    /**
     * get request circuit breaker information of cluster
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return circuit breaker rules
     */
    public Optional<XdsRequestCircuitBreakers> getRequestCircuitBreakers(String serviceName, String clusterName) {
        if (xdsFlowControlService == null || StringUtils.isEmpty(clusterName) || StringUtils.isEmpty(serviceName)) {
            return Optional.empty();
        }
        return xdsFlowControlService.getRequestCircuitBreakers(serviceName, clusterName);
    }

    /**
     * get instance circuit breaker information of cluster
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return circuit breaker rules
     */
    public Optional<XdsInstanceCircuitBreakers> getInstanceCircuitBreakers(String serviceName, String clusterName) {
        if (xdsFlowControlService == null || StringUtils.isEmpty(clusterName) || StringUtils.isEmpty(serviceName)) {
            return Optional.empty();
        }
        return xdsFlowControlService.getInstanceCircuitBreakers(serviceName, clusterName);
    }

    /**
     * get retry policy of route name
     *
     * @param serviceName service name
     * @param routeName route name
     * @return retry policy
     */
    public Optional<XdsRetryPolicy> getRetryPolicy(String serviceName, String routeName) {
        if (xdsFlowControlService == null || StringUtils.isEmpty(routeName) || StringUtils.isEmpty(serviceName)) {
            return Optional.empty();
        }
        return xdsFlowControlService.getRetryPolicy(serviceName, routeName);
    }

    /**
     * get rate limit of route name
     *
     * @param serviceName service name
     * @param routeName route name
     * @param clusterName cluster name
     * @return rate limit rule
     */
    public Optional<XdsRateLimit> getRateLimit(String serviceName, String routeName, String clusterName) {
        if (xdsFlowControlService == null || StringUtils.isEmpty(clusterName) || StringUtils.isEmpty(routeName)
                || StringUtils.isEmpty(serviceName)) {
            return Optional.empty();
        }
        String[] clusterInfo = clusterName.split(CommonConstant.ESCAPED_VERTICAL_LINE);
        if (clusterInfo.length != CLUSTER_NAME_SPLIT_LENGTH) {
            return Optional.empty();
        }
        return xdsFlowControlService.getRateLimit(serviceName, routeName, clusterInfo[1]);
    }

    /**
     * get http fault of route name
     *
     * @param serviceName service name
     * @param routeName route name
     * @return http fault rule
     */
    public Optional<XdsHttpFault> getHttpFault(String serviceName, String routeName) {
        if (xdsFlowControlService == null || StringUtils.isEmpty(routeName) || StringUtils.isEmpty(serviceName)) {
            return Optional.empty();
        }
        return xdsFlowControlService.getHttpFault(serviceName, routeName);
    }

    /**
     * get http fault of route name
     *
     * @param serviceName service name
     * @return route rules
     */
    public List<XdsRoute> getServiceRouteByServiceName(String serviceName) {
        if (xdsRouteService == null || StringUtils.isEmpty(serviceName)) {
            return Collections.emptyList();
        }
        return xdsRouteService.getServiceRoute(serviceName);
    }

    /**
     * get ServiceInstance of service name
     *
     * @param serviceName service name
     * @return route rules
     */
    public Set<ServiceInstance> getServiceInstanceByServiceName(String serviceName) {
        if (xdsServiceDiscovery == null || StringUtils.isEmpty(serviceName)) {
            return Collections.emptySet();
        }
        return xdsServiceDiscovery.getServiceInstance(serviceName);
    }

    /**
     * get ServiceInstance of service name and cluster name
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return lb policy
     */
    public Optional<XdsLbPolicy> getLbPolicyOfCluster(String serviceName, String clusterName) {
        if (xdsLoadBalanceService == null || StringUtils.isEmpty(serviceName) || StringUtils.isEmpty(clusterName)) {
            return Optional.empty();
        }
        return Optional.of(xdsLoadBalanceService.getLbPolicyOfCluster(serviceName, clusterName));
    }

    /**
     * get ServiceInstance of service name and cluster name
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return Service Instance
     */
    public Set<ServiceInstance> getMatchedServiceInstance(String serviceName, String clusterName) {
        if (xdsServiceDiscovery == null || xdsRouteService == null) {
            return Collections.emptySet();
        }
        if (StringUtils.isEmpty(serviceName) || StringUtils.isEmpty(clusterName)) {
            return Collections.emptySet();
        }
        Optional<XdsClusterLoadAssigment> loadAssigmentOptional =
                xdsServiceDiscovery.getClusterServiceInstance(serviceName, clusterName);
        if (!loadAssigmentOptional.isPresent()) {
            return xdsServiceDiscovery.getServiceInstance(serviceName);
        }
        XdsClusterLoadAssigment clusterLoadAssigment = loadAssigmentOptional.get();
        if (!xdsRouteService.isLocalityRoute(serviceName, clusterLoadAssigment.getClusterName())) {
            Set<ServiceInstance> serviceInstances = getServiceInstanceOfCluster(clusterLoadAssigment);
            return serviceInstances.isEmpty() ? xdsServiceDiscovery.getServiceInstance(serviceName) : serviceInstances;
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
        return serviceInstances.isEmpty() ? xdsServiceDiscovery.getServiceInstance(serviceName) : serviceInstances;
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
        for (Map.Entry<XdsLocality, Set<ServiceInstance>> xdsLocalitySetEntry : clusterLoadAssigment
                .getLocalityInstances().entrySet()) {
            serviceInstances.addAll(xdsLocalitySetEntry.getValue());
        }
        return serviceInstances;
    }
}
