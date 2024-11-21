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
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.StringUtils;
import io.sermant.discovery.config.NacosRegisterConfig;
import io.sermant.discovery.entity.RegisterContext;
import io.sermant.discovery.entity.ServiceInstance;
import io.sermant.discovery.service.lb.discovery.ServiceDiscoveryClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * nacos Register plug-in configuration
 *
 * @author xiaozhao
 * @since 2024-11-16
 */
public class NacosDiscoveryClient implements ServiceDiscoveryClient {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final NacosServiceManager nacosServiceManager = NacosServiceManager.getInstance();

    private Instance instance;

    private final NacosRegisterConfig nacosRegisterConfig;

    /**
     * constructor
     */
    public NacosDiscoveryClient() {
        nacosRegisterConfig = PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
    }

    @Override
    public void init() {
    }

    @Override
    public boolean registry(ServiceInstance serviceInstance) {
        String serviceId = serviceInstance.getServiceName();
        String group = nacosRegisterConfig.getGroup();
        instance = nacosServiceManager.buildNacosInstanceFromRegistration(serviceInstance);
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            namingService.registerInstance(serviceId, group, instance);
            return true;
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed when registry service，serviceId={%s}",
                    serviceId), e);
        }
        return false;
    }

    @Override
    public Collection<String> getServices() {
        try {
            String group = nacosRegisterConfig.getGroup();
            NamingService namingService = nacosServiceManager.getNamingService();
            ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, group);
            return services.getData();
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "getServices failed，"
                    + "isFailureToleranceEnabled={%s}", nacosRegisterConfig.isFailureToleranceEnabled()), e);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean unRegistry() {
        if (StringUtils.isEmpty(RegisterContext.INSTANCE.getClientInfo().getServiceId())) {
            LOGGER.warning("No service to de-register for nacos client...");
            return false;
        }
        String serviceId = RegisterContext.INSTANCE.getClientInfo().getServiceId();
        String group = nacosRegisterConfig.getGroup();
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            namingService.deregisterInstance(serviceId, group, instance);
            return true;
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed when deRegister service，"
                    + "serviceId={%s}", serviceId), e);
        }
        return false;
    }

    @Override
    public String name() {
        return "Nacos";
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * Obtain information about the microservice instance corresponding to the service name
     *
     * @param serviceId ServiceId
     * @return Service information
     */
    public List<ServiceInstance> getInstances(String serviceId) {
        String group = nacosRegisterConfig.getGroup();
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            List<Instance> instances = namingService.selectInstances(serviceId, group, true);
            return convertServiceInstanceList(instances, serviceId);
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed get Instances，"
                    + "serviceId={%s}", serviceId), e);
        }
        return Collections.emptyList();
    }

    /**
     * Convert the instance list to service instance list
     *
     * @param instances
     * @param serviceId
     * @return ServiceInstance list
     */
    public List<ServiceInstance> convertServiceInstanceList(List<Instance> instances, String serviceId) {
        List<ServiceInstance> result = new ArrayList<>(instances.size());
        for (Instance nacosInstance : instances) {
            Optional<ServiceInstance> optional = nacosServiceManager.convertServiceInstance(nacosInstance, serviceId);
            optional.ifPresent(result::add);
        }
        return result;
    }
}
