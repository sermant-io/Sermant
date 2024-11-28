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

package io.sermant.core.service.xds;

import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsClusterLoadAssigment;
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;

import java.util.Optional;
import java.util.Set;

/**
 * xds service discovery interface
 *
 * @author daizhenyu
 * @since 2024-05-21
 **/
public interface XdsServiceDiscovery {
    /**
     * get all the service instance by service name with base cluster
     *
     * @param serviceName service name
     * @return service instances
     */
    Set<ServiceInstance> getServiceInstance(String serviceName);

    /**
     * get service instance of service cluster
     *
     * @param clusterName cluster name
     * @return XdsClusterInstance
     */
    Optional<XdsClusterLoadAssigment> getClusterServiceInstance(String clusterName);

    /**
     * get service instance of service cluster
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @return XdsClusterInstance
     */
    Optional<XdsClusterLoadAssigment> getClusterServiceInstance(String serviceName, String clusterName);

    /**
     * subscribe service instance without tag by service name, the listener will be triggered when the service instance
     * changes
     *
     * @param serviceName service name
     * @param listener listener
     */
    void subscribeServiceInstance(String serviceName, XdsServiceDiscoveryListener listener);
}
