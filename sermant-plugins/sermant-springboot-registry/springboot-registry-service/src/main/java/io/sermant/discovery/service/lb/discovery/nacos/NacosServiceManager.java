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

package io.sermant.discovery.service.lb.discovery.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.discovery.config.NacosRegisterConfig;
import io.sermant.discovery.entity.DefaultServiceInstance;
import io.sermant.discovery.entity.ServiceInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * NACOS Registration Service Manager
 *
 * @author xiaozhao
 * @since 2024-11-16
 */
public class NacosServiceManager {
    private static final int DEFAULT_CAPACITY = 16;

    private static NacosServiceManager nacosServiceManager;

    private volatile NamingService namingService;

    private volatile NamingMaintainService namingMaintainService;

    private final NacosRegisterConfig nacosRegisterConfig;

    /**
     * Constructor
     */
    public NacosServiceManager() {
        nacosRegisterConfig = PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
    }

    /**
     * Get the singleton instance
     *
     * @return NacosServiceManager
     */
    public static NacosServiceManager getInstance() {
        if (nacosServiceManager == null) {
            synchronized (NacosServiceManager.class) {
                if (nacosServiceManager == null) {
                    nacosServiceManager = new NacosServiceManager();
                }
            }
        }
        return nacosServiceManager;
    }

    /**
     * Get registration services
     *
     * @return NamingService services
     * @throws NacosException nacos exception
     */
    public NamingService getNamingService() throws NacosException {
        if (Objects.isNull(this.namingService)) {
            buildNamingService(nacosRegisterConfig.getNacosProperties());
        }
        return namingService;
    }

    /**
     * Obtain the namingMaintain service
     *
     * @return namingMaintain service
     * @throws NacosException nacos exception
     */
    public NamingMaintainService getNamingMaintainService() throws NacosException {
        if (Objects.isNull(namingMaintainService)) {
            buildNamingMaintainService(nacosRegisterConfig.getNacosProperties());
        }
        return namingMaintainService;
    }

    private void buildNamingMaintainService(Properties properties) throws NacosException {
        if (Objects.isNull(namingMaintainService)) {
            synchronized (NacosServiceManager.class) {
                if (Objects.isNull(namingMaintainService)) {
                    namingMaintainService = createNamingMaintainService(properties);
                }
            }
        }
    }

    private void buildNamingService(Properties properties) throws NacosException {
        if (Objects.isNull(namingService)) {
            synchronized (NacosServiceManager.class) {
                if (Objects.isNull(namingService)) {
                    namingService = createNewNamingService(properties);
                }
            }
        }
    }

    private NamingService createNewNamingService(Properties properties) throws NacosException {
        return new NacosNamingService(properties);
    }

    private NamingMaintainService createNamingMaintainService(Properties properties) throws NacosException {
        return new NacosNamingMaintainService(properties);
    }

    /**
     * Build the nacos registry instance
     *
     * @param serviceInstance ServiceInstance
     * @return instance
     */
    public Instance buildNacosInstanceFromRegistration(ServiceInstance serviceInstance) {
        Instance instance = new Instance();
        instance.setIp(serviceInstance.getIp());
        instance.setPort(serviceInstance.getPort());
        instance.setWeight(nacosRegisterConfig.getWeight());
        instance.setClusterName(nacosRegisterConfig.getClusterName());
        instance.setEnabled(nacosRegisterConfig.isInstanceEnabled());
        final HashMap<String, String> metadata = new HashMap<>(serviceInstance.getMetadata());
        instance.setMetadata(metadata);
        instance.setEphemeral(nacosRegisterConfig.isEphemeral());
        return instance;
    }

    /**
     * Instance information conversion
     *
     * @param instance Instance
     * @param serviceId id
     * @return Converted instance information
     */
    public Optional<ServiceInstance> convertServiceInstance(Instance instance, String serviceId) {
        if (instance == null || !instance.isEnabled() || !instance.isHealthy()) {
            return Optional.empty();
        }
        DefaultServiceInstance nacosServiceInstance = new DefaultServiceInstance();
        nacosServiceInstance.setHost(instance.getIp());
        nacosServiceInstance.setIp(instance.getIp());
        nacosServiceInstance.setPort(instance.getPort());
        nacosServiceInstance.setServiceName(serviceId);
        nacosServiceInstance.setId(instance.getIp() + ":" + instance.getPort());

        Map<String, String> metadata = new HashMap<>(DEFAULT_CAPACITY);
        metadata.put("nacos.instanceId", instance.getInstanceId());
        metadata.put("nacos.weight", instance.getWeight() + "");
        metadata.put("nacos.healthy", instance.isHealthy() + "");
        metadata.put("nacos.cluster", instance.getClusterName() + "");
        if (instance.getMetadata() != null) {
            metadata.putAll(instance.getMetadata());
        }
        metadata.put("nacos.ephemeral", String.valueOf(instance.isEphemeral()));
        nacosServiceInstance.setMetadata(metadata);

        return Optional.of(nacosServiceInstance);
    }
}
