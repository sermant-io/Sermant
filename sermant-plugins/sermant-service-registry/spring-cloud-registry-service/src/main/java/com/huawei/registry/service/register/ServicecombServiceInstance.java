/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.service.register;

import com.huawei.registry.config.ConfigConstants;
import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.utils.CommonUtils;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;

import java.util.Map;
import java.util.Optional;

/**
 * service COMB Service Information
 *
 * @author zhouss
 * @since 2022-02-17
 */
public class ServicecombServiceInstance implements MicroServiceInstance {
    private final RegisterConfig registerConfig;

    private final MicroserviceInstance microserviceInstance;

    private String ip;

    private int port;

    /**
     * Constructor
     *
     * @param instance Instance information
     */
    public ServicecombServiceInstance(MicroserviceInstance instance) {
        this.microserviceInstance = instance;
        this.registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    @Override
    public String getServiceName() {
        return microserviceInstance.getServiceName();
    }

    @Override
    public String getHost() {
        return registerConfig.isPreferIpAddress() ? getIp() : microserviceInstance.getHostName();
    }

    @Override
    public String getIp() {
        if (ip == null) {
            final Optional<String> ipByEndpoint = CommonUtils.getIpByEndpoint(getRestEndpoint());
            ipByEndpoint.ifPresent(filterIp -> ip = filterIp);
        }
        return ip;
    }

    @Override
    public int getPort() {
        if (port == 0) {
            port = CommonUtils.getPortByEndpoint(getRestEndpoint());
        }
        return port;
    }

    private String getRestEndpoint() {
        return microserviceInstance.getEndpoints().stream()
            .filter(endpoint -> endpoint.startsWith("rest://"))
            .findAny().orElse(null);
    }

    @Override
    public String getServiceId() {
        return microserviceInstance.getServiceId();
    }

    @Override
    public String getInstanceId() {
        return microserviceInstance.getInstanceId();
    }

    @Override
    public Map<String, String> getMetadata() {
        return microserviceInstance.getProperties();
    }

    @Override
    public boolean isSecure() {
        Map<String, String> properties = microserviceInstance.getProperties();
        return Boolean.valueOf(properties.get(ConfigConstants.SECURE));
    }
}
