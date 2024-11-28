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

package io.sermant.router.common.xds.lb;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsLoadBalanceService;
import io.sermant.core.service.xds.entity.XdsLbPolicy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * XdsLoadBalancerFactory
 *
 * @author daizhenyu
 * @since 2024-08-30
 **/
public class XdsLoadBalancerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<String, XdsLoadBalancer> LOAD_BALANCERS = new ConcurrentHashMap<>();

    private static final String RANDOM = "RANDOM";

    private static XdsLoadBalanceService loadBalanceService;

    static {
        XdsCoreService coreService = ServiceManager.getService(XdsCoreService.class);
        if (coreService != null) {
            loadBalanceService = coreService.getLoadBalanceService();
        } else {
            LOGGER.severe("xDS service not open for xDS routing.");
        }
    }

    private XdsLoadBalancerFactory() {
    }

    /**
     * getRoundRobinLoadBalancer
     *
     * @param clusterName cluster name
     * @return XdsLoadBalancer
     */
    private static XdsLoadBalancer getRoundRobinLoadBalancer(String clusterName) {
        return LOAD_BALANCERS.computeIfAbsent(clusterName, key -> new XdsRoundRobinLoadBalancer());
    }

    /**
     * getRandomLoadBalancer
     *
     * @return XdsLoadBalancer
     */
    private static XdsLoadBalancer getRandomLoadBalancer() {
        return LOAD_BALANCERS.computeIfAbsent(RANDOM, key -> new XdsRandomLoadBalancer());
    }

    /**
     * getLoadBalancer
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return XdsLoadBalancer
     */
    public static XdsLoadBalancer getLoadBalancer(String serviceName, String clusterName) {
        if (loadBalanceService == null) {
            LOGGER.severe("xDS service not open for xDS routing.");
            return getRoundRobinLoadBalancer(clusterName);
        }
        XdsLbPolicy lbPolicy = loadBalanceService.getLbPolicyOfCluster(serviceName, clusterName);
        switch (lbPolicy) {
            case RANDOM:
                return getRandomLoadBalancer();
            default:
                return getRoundRobinLoadBalancer(clusterName);
        }
    }
}
