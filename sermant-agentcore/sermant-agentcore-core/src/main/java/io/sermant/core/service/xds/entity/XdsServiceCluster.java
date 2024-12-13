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
import java.util.Optional;
import java.util.Set;

/**
 * XdsServiceCluster contains all XdsCluster data of service
 *
 * @author daizhenyu
 * @since 2024-08-15
 **/
public class XdsServiceCluster {
    /**
     * key:cluster name value:XdsCluster
     */
    private Map<String, XdsCluster> clusters;

    private String baseClusterName;

    public String getBaseClusterName() {
        return baseClusterName;
    }

    public void setBaseClusterName(String baseClusterName) {
        this.baseClusterName = baseClusterName;
    }

    public void setClusters(Map<String, XdsCluster> clusters) {
        this.clusters = clusters;
    }

    /**
     * get service all cluster resource
     *
     * @return clusters
     */
    public Set<String> getClusterResources() {
        if (clusters == null) {
            return Collections.EMPTY_SET;
        }
        return clusters.keySet();
    }

    /**
     * is cluster locality lb
     *
     * @param clusterName cluster name
     * @return boolean
     */
    public boolean isClusterLocalityLb(String clusterName) {
        if (clusters == null) {
            return false;
        }
        XdsCluster xdsCluster = clusters.get(clusterName);
        return xdsCluster != null && xdsCluster.isLocalityLb();
    }

    /**
     * get cluster lb policy
     *
     * @param clusterName cluster name
     * @return XdsLbPolicy
     */
    public XdsLbPolicy getLbPolicyOfCluster(String clusterName) {
        if (clusters == null) {
            return XdsLbPolicy.UNRECOGNIZED;
        }
        XdsCluster xdsCluster = clusters.get(clusterName);
        if (xdsCluster == null) {
            return XdsLbPolicy.UNRECOGNIZED;
        }
        return xdsCluster.getLbPolicy();
    }

    /**
     * get service(base cluster) lb policy
     *
     * @return XdsLbPolicy
     */
    public XdsLbPolicy getBaseLbPolicyOfService() {
        if (clusters == null) {
            return XdsLbPolicy.UNRECOGNIZED;
        }
        XdsCluster xdsCluster = clusters.get(baseClusterName);
        if (xdsCluster == null) {
            return XdsLbPolicy.UNRECOGNIZED;
        }
        return xdsCluster.getLbPolicy();
    }

    /**
     * get XdsRequestCircuitBreakers
     *
     * @param clusterName cluster name
     * @return XdsOutlierDetection
     */
    public Optional<XdsRequestCircuitBreakers> getRequestCircuitBreakersOfCluster(String clusterName) {
        if (clusters == null) {
            return Optional.empty();
        }
        XdsCluster xdsCluster = clusters.get(clusterName);
        if (xdsCluster == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(xdsCluster.getRequestCircuitBreakers());
    }

    /**
     * get XdsInstanceCircuitBreakers
     *
     * @param clusterName cluster name
     * @return XdsOutlierDetection
     */
    public Optional<XdsInstanceCircuitBreakers> getInstanceCircuitBreakersOfCluster(String clusterName) {
        if (clusters == null) {
            return Optional.empty();
        }
        XdsCluster xdsCluster = clusters.get(clusterName);
        if (xdsCluster == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(xdsCluster.getInstanceCircuitBreakers());
    }
}
