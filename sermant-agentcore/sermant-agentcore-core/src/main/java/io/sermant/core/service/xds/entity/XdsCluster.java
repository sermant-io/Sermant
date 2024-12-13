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

/**
 * XdsCluster corresponds to io.envoyproxy.envoy.config.cluster.v3.Cluster
 *
 * @author daizhenyu
 * @since 2024-08-06
 **/
public class XdsCluster {
    private String clusterName;

    private String serviceName;

    private XdsLbPolicy lbPolicy;

    private boolean isLocalityLb;

    private XdsRequestCircuitBreakers requestCircuitBreakers;

    private XdsInstanceCircuitBreakers instanceCircuitBreakers;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public XdsLbPolicy getLbPolicy() {
        return lbPolicy;
    }

    public void setLbPolicy(XdsLbPolicy lbPolicy) {
        this.lbPolicy = lbPolicy;
    }

    public boolean isLocalityLb() {
        return isLocalityLb;
    }

    public void setLocalityLb(boolean localityLb) {
        isLocalityLb = localityLb;
    }

    public XdsRequestCircuitBreakers getRequestCircuitBreakers() {
        return requestCircuitBreakers;
    }

    public void setRequestCircuitBreakers(XdsRequestCircuitBreakers requestCircuitBreakers) {
        this.requestCircuitBreakers = requestCircuitBreakers;
    }

    public XdsInstanceCircuitBreakers getInstanceCircuitBreakers() {
        return instanceCircuitBreakers;
    }

    public void setInstanceCircuitBreakers(XdsInstanceCircuitBreakers instanceCircuitBreakers) {
        this.instanceCircuitBreakers = instanceCircuitBreakers;
    }
}
