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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * XdsServiceClusterLoadAssigment contains all XdsClusterLoadAssigment data of service
 *
 * @author daizhenyu
 * @since 2024-08-15
 **/
public class XdsServiceClusterLoadAssigment {
    /**
     * key:cluster name value:XdsClusterLoadAssigment
     */
    private Map<String, XdsClusterLoadAssigment> clusterLoadAssigments;

    private String baseClusterName;

    public Map<String, XdsClusterLoadAssigment> getClusterLoadAssigments() {
        return clusterLoadAssigments;
    }

    public void setClusterLoadAssigments(Map<String, XdsClusterLoadAssigment> clusterLoadAssigments) {
        this.clusterLoadAssigments = clusterLoadAssigments;
    }

    public String getBaseClusterName() {
        return baseClusterName;
    }

    public void setBaseClusterName(String baseClusterName) {
        this.baseClusterName = baseClusterName;
    }

    /**
     * get all service instance
     *
     * @return service instances
     */
    public Set<ServiceInstance> getServiceInstance() {
        return getClusterServiceInstance(baseClusterName);
    }

    /**
     * get service instance of service cluster
     *
     * @param clusterName cluster name
     * @return service instances
     */
    public Set<ServiceInstance> getServiceInstance(String clusterName) {
        return getClusterServiceInstance(clusterName);
    }

    /**
     * get XdsClusterLoadAssigment
     *
     * @param clusterName cluster name
     * @return XdsClusterLoadAssigment
     */
    public XdsClusterLoadAssigment getXdsClusterLoadAssigment(String clusterName) {
        return clusterLoadAssigments.get(clusterName);
    }

    private Set<ServiceInstance> getClusterServiceInstance(String clusterName) {
        if (clusterLoadAssigments == null) {
            return Collections.EMPTY_SET;
        }
        XdsClusterLoadAssigment xdsClusterLoadAssigment = clusterLoadAssigments.get(clusterName);
        if (xdsClusterLoadAssigment == null) {
            return Collections.EMPTY_SET;
        }
        Map<XdsLocality, Set<ServiceInstance>> localityInstances = xdsClusterLoadAssigment.getLocalityInstances();
        if (localityInstances == null) {
            return Collections.EMPTY_SET;
        }
        return localityInstances.entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
    }
}
