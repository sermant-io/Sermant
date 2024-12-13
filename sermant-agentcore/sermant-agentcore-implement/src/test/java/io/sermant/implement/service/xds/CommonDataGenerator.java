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

package io.sermant.implement.service.xds;

import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsAbort;
import io.sermant.core.service.xds.entity.XdsCluster;
import io.sermant.core.service.xds.entity.XdsClusterLoadAssigment;
import io.sermant.core.service.xds.entity.XdsHttpConnectionManager;
import io.sermant.core.service.xds.entity.XdsHttpFault;
import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.service.xds.entity.XdsRateLimit;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.service.xds.entity.XdsRouteConfiguration;
import io.sermant.core.service.xds.entity.XdsServiceCluster;
import io.sermant.core.service.xds.entity.XdsServiceClusterLoadAssigment;
import io.sermant.core.service.xds.entity.XdsTokenBucket;
import io.sermant.core.service.xds.entity.XdsVirtualHost;
import io.sermant.implement.service.xds.entity.XdsServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author daizhenyu
 * @since 2024-08-26
 **/
public class CommonDataGenerator {
    public static List<XdsRouteConfiguration> createRouteConfigurations() {
        XdsRouteConfiguration routeConfiguration = new XdsRouteConfiguration();
        XdsVirtualHost virtualHost1 = new XdsVirtualHost();
        virtualHost1.setName("serviceA.name:8080");
        XdsVirtualHost virtualHost2 = new XdsVirtualHost();
        virtualHost2.setName("serviceB.name:8080");

        XdsRoute route = new XdsRoute();
        route.setName("test-route");
        XdsHttpFault httpFault = new XdsHttpFault();
        httpFault.setAbort(new XdsAbort());
        httpFault.getAbort().setHttpStatus(503);
        route.setHttpFault(httpFault);
        XdsRateLimit xdsRateLimit = new XdsRateLimit();
        XdsTokenBucket xdsTokenBucket = new XdsTokenBucket();
        xdsTokenBucket.setMaxTokens(10);
        xdsRateLimit.setTokenBucket(xdsTokenBucket);
        route.setRateLimit(xdsRateLimit);
        virtualHost1.setRoutes(Collections.singletonList(route));
        virtualHost2.setRoutes(Collections.emptyList());
        Map<String, XdsVirtualHost> virtualHosts = new HashMap<>();
        virtualHosts.put("serviceA", virtualHost1);
        virtualHosts.put("serviceB", virtualHost2);
        routeConfiguration.setVirtualHosts(virtualHosts);
        return Collections.singletonList(routeConfiguration);
    }

    public static Map<String, XdsServiceCluster> createServiceClusterMap(String serviceName, String clusterName) {
        XdsCluster cluster = initCluster(clusterName);
        Map<String, XdsCluster> clusters = new HashMap<>();
        clusters.put(clusterName, cluster);

        XdsServiceCluster serviceCluster = new XdsServiceCluster();
        serviceCluster.setClusters(clusters);
        serviceCluster.setBaseClusterName(clusterName);

        Map<String, XdsServiceCluster> serviceClusterMap = new HashMap<>();
        serviceClusterMap.put(serviceName, serviceCluster);
        return serviceClusterMap;
    }

    private static XdsCluster initCluster(String clusterName) {
        XdsCluster cluster = new XdsCluster();
        cluster.setClusterName(clusterName);
        cluster.setLocalityLb(true);
        cluster.setLbPolicy(XdsLbPolicy.RANDOM);
        XdsRequestCircuitBreakers requestCircuitBreakers = new XdsRequestCircuitBreakers();
        requestCircuitBreakers.setMaxRequests(100);
        cluster.setRequestCircuitBreakers(requestCircuitBreakers);
        XdsInstanceCircuitBreakers instanceCircuitBreakers = new XdsInstanceCircuitBreakers();
        instanceCircuitBreakers.setInterval(1000);
        cluster.setInstanceCircuitBreakers(instanceCircuitBreakers);
        return cluster;
    }

    public static XdsServiceClusterLoadAssigment createXdsServiceClusterInstance(List<String> clusterNames,
            String baseClusterName) {
        Map<String, XdsClusterLoadAssigment> clusterInstances = new HashMap<>();
        for (String clusterName : clusterNames) {
            Set<ServiceInstance> instances = new HashSet<>();
            instances.add(new XdsServiceInstance());

            XdsLocality locality = new XdsLocality();
            Map<XdsLocality, Set<ServiceInstance>> localityInstances = new HashMap<>();
            localityInstances.put(locality, instances);

            XdsClusterLoadAssigment clusterInstance = new XdsClusterLoadAssigment();
            clusterInstance.setClusterName(clusterName);
            clusterInstance.setLocalityInstances(localityInstances);
            clusterInstances.put(clusterName, clusterInstance);
        }

        XdsServiceClusterLoadAssigment serviceClusterInstance = new XdsServiceClusterLoadAssigment();
        serviceClusterInstance.setBaseClusterName(baseClusterName);
        serviceClusterInstance.setClusterLoadAssigments(clusterInstances);
        return serviceClusterInstance;
    }

    public static List<XdsHttpConnectionManager> createHttpConnectionManagers(List<String> routerConfigs) {
        List<XdsHttpConnectionManager> managers = new ArrayList<>();
        for (String routerConfig : routerConfigs) {
            XdsHttpConnectionManager manager = new XdsHttpConnectionManager();
            manager.setRouteConfigName("route-test");
            managers.add(manager);
        }
        return managers;
    }
}
