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

package io.sermant.implement.service.xds.cache;

import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.grpc.stub.StreamObserver;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsClusterLoadAssigment;
import io.sermant.core.service.xds.entity.XdsHttpConnectionManager;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.core.service.xds.entity.XdsRouteConfiguration;
import io.sermant.core.service.xds.entity.XdsServiceCluster;
import io.sermant.core.service.xds.entity.XdsServiceClusterLoadAssigment;
import io.sermant.core.service.xds.entity.XdsVirtualHost;
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * xDS data cache
 *
 * @author daizhenyu
 * @since 2024-05-09
 **/
public class XdsDataCache {
    /**
     * key:service name value:instances
     */
    private static final Map<String, XdsServiceClusterLoadAssigment> SERVICE_INSTANCES =
            new ConcurrentHashMap<>();

    /**
     * key:service name value:listener list
     */
    private static final Map<String, List<XdsServiceDiscoveryListener>> SERVICE_DISCOVER_LISTENER =
            new ConcurrentHashMap<>();

    /**
     * request StreamObserver
     */
    private static final Map<String, StreamObserver<DiscoveryRequest>> REQUEST_OBSERVERS = new ConcurrentHashMap<>();

    /**
     * key:service name value:XdsServiceCluster
     */
    private static Map<String, XdsServiceCluster> serviceClusterMap = new HashMap<>();

    /**
     * HttpConnectionManager
     */
    private static List<XdsHttpConnectionManager> httpConnectionManagers = new ArrayList<>();

    /**
     * XdsRouteConfiguration
     */
    private static List<XdsRouteConfiguration> routeConfigurations = new ArrayList<>();

    private XdsDataCache() {
    }

    /**
     * update ServiceInstance
     *
     * @param serviceName service name
     * @param serviceClusterInstance all cluster instance of service
     */
    public static void updateServiceInstance(String serviceName,
            XdsServiceClusterLoadAssigment serviceClusterInstance) {
        SERVICE_INSTANCES.put(serviceName, serviceClusterInstance);
    }

    /**
     * get ServiceInstance
     *
     * @param serviceName service name
     * @return ServiceInstance
     */
    public static Set<ServiceInstance> getServiceInstance(String serviceName) {
        XdsServiceClusterLoadAssigment serviceClusterInstance = SERVICE_INSTANCES.get(serviceName);
        if (serviceClusterInstance == null) {
            return Collections.emptySet();
        }
        return serviceClusterInstance.getServiceInstance();
    }

    /**
     * get ServiceInstance of cluster
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return ServiceInstance
     */
    public static Optional<XdsClusterLoadAssigment> getClusterServiceInstance(String serviceName,
            String clusterName) {
        XdsServiceClusterLoadAssigment serviceClusterInstance = SERVICE_INSTANCES.get(serviceName);
        if (serviceClusterInstance == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(serviceClusterInstance.getXdsClusterLoadAssigment(clusterName));
    }

    /**
     * remove ServiceInstance
     *
     * @param serviceName service name
     */
    public static void removeServiceInstance(String serviceName) {
        SERVICE_INSTANCES.remove(serviceName);
    }

    /**
     * add ServiceDiscoveryListener
     *
     * @param serviceName service name
     * @param listener listener
     */
    public static void addServiceDiscoveryListener(String serviceName, XdsServiceDiscoveryListener listener) {
        SERVICE_DISCOVER_LISTENER.computeIfAbsent(serviceName, value -> new ArrayList<>())
                .add(listener);
    }

    /**
     * get ServiceDiscoveryListeners
     *
     * @param serviceName service name
     * @return ServiceDiscoveryListeners
     */
    public static List<XdsServiceDiscoveryListener> getServiceDiscoveryListeners(String serviceName) {
        return SERVICE_DISCOVER_LISTENER.getOrDefault(serviceName, Collections.emptyList());
    }

    /**
     * remove ServiceDiscoveryListeners
     *
     * @param serviceName service name
     */
    public static void removeServiceDiscoveryListeners(String serviceName) {
        SERVICE_DISCOVER_LISTENER.remove(serviceName);
    }

    /**
     * update RequestObserver
     *
     * @param serviceName service name
     * @param requestObserver request observer
     */
    public static void updateRequestObserver(String serviceName, StreamObserver<DiscoveryRequest> requestObserver) {
        REQUEST_OBSERVERS.put(serviceName, requestObserver);
    }

    /**
     * Whether the service's request observer exists
     *
     * @param serviceName service name
     * @return boolean
     */
    public static boolean isContainsRequestObserver(String serviceName) {
        return REQUEST_OBSERVERS.containsKey(serviceName);
    }

    /**
     * get request observers entry
     *
     * @return request observer
     */
    public static Set<Entry<String, StreamObserver<DiscoveryRequest>>> getRequestObserversEntry() {
        return REQUEST_OBSERVERS.entrySet();
    }

    /**
     * get request observer by request key
     *
     * @param requestKey request key
     * @return request observer
     */
    public static StreamObserver<DiscoveryRequest> getRequestObserver(String requestKey) {
        return REQUEST_OBSERVERS.get(requestKey);
    }

    /**
     * remove request observer by service name
     *
     * @param serviceName service name
     */
    public static void removeRequestObserver(String serviceName) {
        REQUEST_OBSERVERS.remove(serviceName);
    }

    /**
     * update the mapping between service and cluster
     *
     * @param clusterMap the map between service and cluster
     */
    public static void updateServiceClusterMap(Map<String, XdsServiceCluster> clusterMap) {
        if (clusterMap == null) {
            serviceClusterMap = new HashMap<>();
            return;
        }
        serviceClusterMap = clusterMap;
    }

    /**
     * get cluster set for service
     *
     * @param serviceName service name
     * @return cluster set for service
     */
    public static Set<String> getClustersByServiceName(String serviceName) {
        XdsServiceCluster xdsServiceCluster = serviceClusterMap.get(serviceName);
        if (xdsServiceCluster == null) {
            return Collections.emptySet();
        }
        return xdsServiceCluster.getClusterResources();
    }

    /**
     * get serviceClusterMap
     *
     * @return serviceClusterMap
     */
    public static Map<String, XdsServiceCluster> getServiceClusterMap() {
        return serviceClusterMap;
    }

    /**
     * update HttpConnectionManager
     *
     * @param hcms httpConnectionManager list
     */
    public static void updateHttpConnectionManagers(List<XdsHttpConnectionManager> hcms) {
        if (hcms == null) {
            httpConnectionManagers = new ArrayList<>();
            return;
        }
        httpConnectionManagers = hcms;
    }

    /**
     * get RouteResources
     *
     * @return RouteConfig names
     */
    public static Set<String> getRouteResources() {
        return httpConnectionManagers.stream()
                .map(XdsHttpConnectionManager::getRouteConfigName)
                .collect(Collectors.toSet());
    }

    /**
     * update XdsRouteConfiguration
     *
     * @param configurations XdsRouteConfiguration list
     */
    public static void updateRouteConfigurations(List<XdsRouteConfiguration> configurations) {
        if (configurations == null) {
            routeConfigurations = new ArrayList<>();
            return;
        }
        routeConfigurations = configurations;
    }

    /**
     * get service route rule
     *
     * @param serviceName service name
     * @return xds route
     */
    public static List<XdsRoute> getServiceRoute(String serviceName) {
        for (XdsRouteConfiguration routeConfiguration : routeConfigurations) {
            Map<String, XdsVirtualHost> virtualHosts = routeConfiguration.getVirtualHosts();
            if (virtualHosts.containsKey(serviceName)) {
                return virtualHosts.get(serviceName).getRoutes();
            }
        }
        return Collections.emptyList();
    }

    /**
     * cluster locality lb policy
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return boolean
     */
    public static boolean isLocalityLb(String serviceName, String clusterName) {
        XdsServiceCluster serviceCluster = serviceClusterMap.get(serviceName);
        return serviceCluster != null && serviceCluster.isClusterLocalityLb(clusterName);
    }

    /**
     * cluster lb policy
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return boolean
     */
    public static XdsLbPolicy getLbPolicyOfCluster(String serviceName, String clusterName) {
        XdsServiceCluster serviceCluster = serviceClusterMap.get(serviceName);
        if (serviceCluster == null) {
            return XdsLbPolicy.UNRECOGNIZED;
        }
        return serviceCluster.getLbPolicyOfCluster(clusterName);
    }

    /**
     * get service(base cluster) lb policy
     *
     * @param serviceName service name
     * @return boolean
     */
    public static XdsLbPolicy getBaseLbPolicyOfService(String serviceName) {
        XdsServiceCluster serviceCluster = serviceClusterMap.get(serviceName);
        if (serviceCluster == null) {
            return XdsLbPolicy.UNRECOGNIZED;
        }
        return serviceCluster.getBaseLbPolicyOfService();
    }

    /**
     * getRouteConfigurations
     *
     * @return XdsRouteConfiguration list
     */
    public static List<XdsRouteConfiguration> getRouteConfigurations() {
        return routeConfigurations;
    }
}
