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

package io.sermant.implement.service.xds.flowcontrol;

import io.sermant.core.service.xds.XdsFlowControlService;
import io.sermant.core.service.xds.entity.XdsHttpFault;
import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRateLimit;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRetryPolicy;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.service.xds.entity.XdsRouteAction;
import io.sermant.core.service.xds.entity.XdsRouteConfiguration;
import io.sermant.core.service.xds.entity.XdsServiceCluster;
import io.sermant.core.service.xds.entity.XdsVirtualHost;
import io.sermant.core.utils.StringUtils;
import io.sermant.implement.service.xds.cache.XdsDataCache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The implementation class of XdsFlowControlService
 *
 * @author zhp
 * @since 2024-11-27
 **/
public class XdsFlowControlServiceImpl implements XdsFlowControlService {
    /**
     * constructor
     */
    public XdsFlowControlServiceImpl() {
    }

    @Override
    public Optional<XdsRequestCircuitBreakers> getRequestCircuitBreakers(String serviceName, String clusterName) {
        Map<String, XdsServiceCluster> serviceClusterMap = XdsDataCache.getServiceClusterMap();
        XdsServiceCluster serviceCluster = serviceClusterMap.get(serviceName);
        if (serviceCluster == null) {
            return Optional.empty();
        }
        return serviceCluster.getRequestCircuitBreakersOfCluster(clusterName);
    }

    @Override
    public Optional<XdsInstanceCircuitBreakers> getInstanceCircuitBreakers(String serviceName, String clusterName) {
        Map<String, XdsServiceCluster> serviceClusterMap = XdsDataCache.getServiceClusterMap();
        XdsServiceCluster serviceCluster = serviceClusterMap.get(serviceName);
        if (serviceCluster == null) {
            return Optional.empty();
        }
        return serviceCluster.getInstanceCircuitBreakersOfCluster(clusterName);
    }

    @Override
    public Optional<XdsRetryPolicy> getRetryPolicy(String serviceName, String routeName) {
        List<XdsRoute> xdsRoutes = XdsDataCache.getServiceRoute(serviceName);
        for (XdsRoute xdsRoute : xdsRoutes) {
            if (StringUtils.equals(xdsRoute.getName(), routeName)) {
                XdsRouteAction routeAction = xdsRoute.getRouteAction();
                return routeAction == null ? Optional.empty() : Optional.ofNullable(routeAction.getRetryPolicy());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<XdsRateLimit> getRateLimit(String serviceName, String routeName, String port) {
        for (XdsRouteConfiguration routeConfiguration : XdsDataCache.getRouteConfigurations()) {
            Map<String, XdsVirtualHost> virtualHosts = routeConfiguration.getVirtualHosts();
            if (!virtualHosts.containsKey(serviceName)) {
                continue;
            }
            XdsVirtualHost virtualHost = virtualHosts.get(serviceName);
            if (virtualHost == null || StringUtils.isEmpty(virtualHost.getName())
                    || !virtualHost.getName().contains(port)) {
                continue;
            }
            for (XdsRoute xdsRoute : virtualHost.getRoutes()) {
                if (StringUtils.equals(xdsRoute.getName(), routeName)) {
                    return Optional.ofNullable(xdsRoute.getRateLimit());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<XdsHttpFault> getHttpFault(String serviceName, String routeName) {
        List<XdsRoute> xdsRoutes = XdsDataCache.getServiceRoute(serviceName);
        for (XdsRoute xdsRoute : xdsRoutes) {
            if (StringUtils.equals(xdsRoute.getName(), routeName)) {
                return Optional.ofNullable(xdsRoute.getHttpFault());
            }
        }
        return Optional.empty();
    }
}
