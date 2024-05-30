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

package io.sermant.implement.service.xds.entity;

import io.sermant.core.service.xds.entity.ServiceInstance;

import java.util.Map;
import java.util.Objects;

/**
 * XdsServiceInstance
 *
 * @author daizhenyu
 * @since 2024-05-10
 **/
public class XdsServiceInstance implements ServiceInstance {
    private String cluster;

    private String service;

    private String host;

    private int port;

    private boolean healthStatus;

    private Map<String, String> metadata;

    @Override
    public String getClusterName() {
        return cluster;
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Map<String, String> getMetaData() {
        return metadata;
    }

    @Override
    public boolean isHealthy() {
        return healthStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        XdsServiceInstance instance = (XdsServiceInstance) obj;
        return port == instance.port && healthStatus == instance.healthStatus
                && Objects.equals(cluster, instance.cluster)
                && Objects.equals(service, instance.service)
                && Objects.equals(host, instance.host)
                && Objects.equals(metadata, instance.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cluster, service, host, port, healthStatus, metadata);
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHealthStatus(boolean healthStatus) {
        this.healthStatus = healthStatus;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
