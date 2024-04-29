/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.registry.service.client;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.registry.config.ConfigConstants;
import io.sermant.registry.config.NacosRegisterConfig;
import io.sermant.registry.service.register.NacosServiceInstance;
import io.sermant.registry.service.register.NacosServiceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Nacos registers the implementation client
 *
 * @since 2022-10-20
 */
public class NacosServiceDiscovery {
    private static final int DEFAULT_CAPACITY = 16;

    private final NacosRegisterConfig nacosRegisterConfig;

    private final NacosServiceManager nacosServiceManager;

    /**
     * Constructor
     *
     * @param nacosServiceManager Naming Service Management
     */
    public NacosServiceDiscovery(NacosServiceManager nacosServiceManager) {
        this.nacosServiceManager = nacosServiceManager;
        nacosRegisterConfig = PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
    }

    /**
     * Obtain the microservice instance information of the corresponding service name
     *
     * @param serviceId Service ID
     * @return Service Information
     * @throws NacosException nacos exception
     */
    public List<NacosServiceInstance> getInstances(String serviceId) throws NacosException {
        String group = nacosRegisterConfig.getGroup();
        NamingService namingService = nacosServiceManager.getNamingService();
        List<Instance> instances = namingService.selectInstances(serviceId, group, true);
        return convertServiceInstanceList(instances, serviceId);
    }

    /**
     * Instance collection information conversion
     *
     * @param instances A collection of instances
     * @param serviceId Service ID
     * @return Information about the collection of converted instances
     */
    public List<NacosServiceInstance> convertServiceInstanceList(List<Instance> instances, String serviceId) {
        List<NacosServiceInstance> result = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            Optional<NacosServiceInstance> optional = convertServiceInstance(instance, serviceId);
            optional.ifPresent(result::add);
        }
        return result;
    }

    /**
     * Instance information conversion
     *
     * @param instance Service instances
     * @param serviceId Service ID
     * @return Information about the converted instance
     */
    public Optional<NacosServiceInstance> convertServiceInstance(Instance instance, String serviceId) {
        if (instance == null || !instance.isEnabled() || !instance.isHealthy()) {
            return Optional.empty();
        }
        NacosServiceInstance nacosServiceInstance = new NacosServiceInstance();
        nacosServiceInstance.setHost(instance.getIp());
        nacosServiceInstance.setPort(instance.getPort());
        nacosServiceInstance.setServiceId(serviceId);
        nacosServiceInstance.setInstanceId(instance.getInstanceId());

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

        if (metadata.containsKey(ConfigConstants.SECURE)) {
            boolean secure = Boolean.parseBoolean(metadata.get(ConfigConstants.SECURE));
            nacosServiceInstance.setSecure(secure);
        }
        return Optional.of(nacosServiceInstance);
    }

    /**
     * Get all service names
     *
     * @return A collection of service names
     * @throws NacosException NACOS abnormality
     */
    public List<String> getServices() throws NacosException {
        String group = nacosRegisterConfig.getGroup();
        NamingService namingService = nacosServiceManager.getNamingService();
        ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, group);
        return services.getData();
    }
}
