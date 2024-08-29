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

/**
 * TestServiceInstance
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class TestServiceInstance implements ServiceInstance{
    public TestServiceInstance() {

    }
    public TestServiceInstance(String cluster, String service, String host, int port) {
        this.cluster = cluster;
        this.service = service;
        this.host = host;
        this.port = port;
    }

    private String cluster;

    private String service;

    private String host;

    private int port;

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
        return null;
    }

    @Override
    public boolean isHealthy() {
        return false;
    }
}
