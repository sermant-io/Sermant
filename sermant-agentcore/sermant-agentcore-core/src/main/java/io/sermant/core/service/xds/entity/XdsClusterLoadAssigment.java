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

package io.sermant.core.service.xds.entity;

import java.util.Map;
import java.util.Set;

/**
 * XdsClusterLoadAssigment corresponds to io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment
 *
 * @author daizhenyu
 * @since 2024-08-15
 **/
public class XdsClusterLoadAssigment {
    private String serviceName;

    private String clusterName;

    private Map<XdsLocality, Set<ServiceInstance>> localityInstances;

    /**
     * constructor
     */
    public XdsClusterLoadAssigment() {
    }

    /**
     * parameterized constructor
     *
     * @param serviceName service name
     * @param clusterName cluster name
     * @param localityInstances service instances sorted by locality
     */
    public XdsClusterLoadAssigment(String serviceName, String clusterName,
            Map<XdsLocality, Set<ServiceInstance>> localityInstances) {
        this.serviceName = serviceName;
        this.clusterName = clusterName;
        this.localityInstances = localityInstances;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<XdsLocality, Set<ServiceInstance>> getLocalityInstances() {
        return localityInstances;
    }

    public void setLocalityInstances(
            Map<XdsLocality, Set<ServiceInstance>> localityInstances) {
        this.localityInstances = localityInstances;
    }
}
