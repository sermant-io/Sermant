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
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    public static final Map<String, Set<ServiceInstance>> SERVICE_INSTANCES =
            new ConcurrentHashMap<>();

    /**
     * key:service name value:listener list
     */
    public static final Map<String, List<XdsServiceDiscoveryListener>> SERVICE_DISCOVER_LISTENER =
            new ConcurrentHashMap<>();

    /**
     * request StreamObserver
     */
    public static final Map<String, StreamObserver<DiscoveryRequest>> REQUEST_OBSERVERS = new ConcurrentHashMap<>();

    /**
     * key:service name value:cluster map
     */
    private static Map<String, Set<String>> serviceNameMapping = new HashMap<>();

    private XdsDataCache() {
    }

    /**
     * update the mapping between service and cluster
     *
     * @param mapping the mapping between service and cluster
     */
    public static void updateServiceNameMapping(Map<String, Set<String>> mapping) {
        if (mapping == null) {
            serviceNameMapping = new HashMap<>();
        }
        serviceNameMapping = mapping;
    }

    /**
     * get cluster set for service
     *
     * @param serviceName
     * @return cluster set for service
     */
    public static Set<String> getClustersByServiceName(String serviceName) {
        return serviceNameMapping.getOrDefault(serviceName, Collections.EMPTY_SET);
    }

    /**
     * get serviceNameMapping
     *
     * @return serviceNameMapping
     */
    public static Map<String, Set<String>> getServiceNameMapping() {
        return serviceNameMapping;
    }
}