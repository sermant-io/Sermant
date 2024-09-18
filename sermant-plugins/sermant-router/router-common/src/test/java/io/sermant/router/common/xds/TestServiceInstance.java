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

package io.sermant.router.common.xds;

import io.sermant.core.service.xds.entity.ServiceInstance;

import java.util.Map;

/**
 * TestServiceInstance
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class TestServiceInstance implements ServiceInstance {
    public TestServiceInstance() {
    }

    private String cluster;

    private String service;

    private String host;

    private int port;

    private Map<String, String> metaData;

    private boolean healthy;

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
        return metaData;
    }

    @Override
    public boolean isHealthy() {
        return healthy;
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

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}
