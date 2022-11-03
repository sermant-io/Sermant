/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.service.client;

import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.service.register.NacosServiceInstance;
import com.huawei.registry.service.register.NacosServiceManager;
import com.huawei.registry.service.register.NacosWatch;
import com.huawei.registry.service.register.ServiceCache;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * nacos注册实现客户端
 *
 * @author chengyouling
 * @since 2022-10-20
 */
public class NacosClient {
    private static final String STATUS_UP = "UP";

    private static final String STATUS_DOWN = "DOWN";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final NacosServiceManager nacosServiceManager;

    private final NacosRegisterConfig nacosRegisterConfig;

    private NacosServiceDiscovery nacosServiceDiscovery;

    /**
     * 构造方法
     */
    public NacosClient() {
        nacosRegisterConfig = PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
        nacosServiceManager = new NacosServiceManager(nacosRegisterConfig);
        nacosServiceDiscovery = new NacosServiceDiscovery(nacosServiceManager);
        if (nacosRegisterConfig.isWatchEnabled()) {
            new NacosWatch(nacosServiceManager, nacosRegisterConfig,
                    RegisterContext.INSTANCE.getClientInfo().getServiceId());
        }
    }

    /**
     * 微服务注册
     */
    public void register() {

        if (StringUtils.isEmpty(RegisterContext.INSTANCE.getClientInfo().getServiceId())) {
            LOGGER.warning("No service to register for nacos client...");
            return;
        }

        NamingService namingService = nacosServiceManager.getNamingService();
        String serviceId = RegisterContext.INSTANCE.getClientInfo().getServiceId();
        String group = nacosRegisterConfig.getGroup();

        Instance instance = nacosServiceManager.buildNacosInstanceFromRegistration();

        try {
            namingService.registerInstance(serviceId, group, instance);
            LOGGER.log(Level.INFO,String.format("nacos registry, {} {} {}:{} register finished", group, serviceId,
                    instance.getIp(), instance.getPort()));
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "nacos registry, "
                    + "{} register failed...", serviceId), e);
        }
    }

    /**
     * 下线处理
     */
    public void deRegister() {
        LOGGER.log(Level.INFO,"De-registering from Nacos Server now...");

        if (StringUtils.isEmpty(RegisterContext.INSTANCE.getClientInfo().getServiceId())) {
            LOGGER.warning("No dom to de-register for nacos client...");
            return;
        }
        NamingService namingService = nacosServiceManager.getNamingService();
        String serviceId = RegisterContext.INSTANCE.getClientInfo().getServiceId();
        String group = nacosRegisterConfig.getGroup();
        try {
            namingService.deregisterInstance(serviceId, group, RegisterContext.INSTANCE.getClientInfo().getIp(),
                    RegisterContext.INSTANCE.getClientInfo().getPort(), nacosRegisterConfig.getClusterName());
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "ERR_NACOS_DEREGISTER, "
                    + "{} de-register failed...", serviceId), e);
        }
        LOGGER.log(Level.INFO,"De-registration finished.");
    }

    /**
     * 获取nacos服务状态
     *
     * @return 服务状态
     */
    public String getServerStatus() {
        NamingService namingService = nacosServiceManager.getNamingService();
        return namingService.getServerStatus();
    }

    /**
     * 更新微服务实例状态
     *
     * @param status
     */
    public void updateInstanceStatus(String status) {
        if (!STATUS_UP.equalsIgnoreCase(status)
                && !STATUS_DOWN.equalsIgnoreCase(status)) {
            LOGGER.warning(String.format(Locale.ENGLISH,"can't support status {},please choose UP or DOWN", status));
            return;
        }

        String serviceId = RegisterContext.INSTANCE.getClientInfo().getServiceId();
        String group = nacosRegisterConfig.getGroup();

        Instance instance = nacosServiceManager.buildNacosInstanceFromRegistration();

        if (STATUS_DOWN.equalsIgnoreCase(status)) {
            instance.setEnabled(false);
        }
        else {
            instance.setEnabled(true);
        }

        try {
            nacosServiceManager.getNamingMaintainService().updateInstance(serviceId, group, instance);
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "{} update nacos instance status fail...",
                    serviceId), e);
        }
    }

    /**
     * 获取微服务实例状态
     *
     * @return 实例状态
     */
    public String getInstanceStatus() {
        NamingService namingService = nacosServiceManager.getNamingService();
        String serviceId = RegisterContext.INSTANCE.getClientInfo().getServiceId();
        String group = nacosRegisterConfig.getGroup();
        try {
            List<Instance> instances = namingService.getAllInstances(serviceId, group);
            for (Instance instance : instances) {
                if (instance.getIp().equalsIgnoreCase(RegisterContext.INSTANCE.getClientInfo().getIp())
                        && instance.getPort() == RegisterContext.INSTANCE.getClientInfo().getPort()) {
                    return instance.isEnabled() ? "UP" : "DOWN";
                }
            }
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "get all instance of {} error...",
                    serviceId), e);
        }
        return "";
    }

    /**
     * 根据服务id获取服务实例集合
     *
     * @param serviceId
     * @return 服务实例列表
     */
    public List<NacosServiceInstance> getInstances(String serviceId) {
        try {
            return Optional.of(nacosServiceDiscovery.getInstances(serviceId))
                    .map(instances -> {
                        ServiceCache.setInstances(serviceId, instances);
                        return instances;
                    }).get();
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "Can not get hosts from nacos server. "
                            + "serviceId： {}，isFailureToleranceEnabled：{}", serviceId,
                    nacosRegisterConfig.isFailureToleranceEnabled()), e);
            if (nacosRegisterConfig.isFailureToleranceEnabled()) {
                return ServiceCache.getInstances(serviceId);
            }
            return Collections.emptyList();
        }
    }

    /**
     * 获取服务名称
     *
     * @return 服务名称集合
     */
    public List<String> getServices() {
        try {
            return Optional.of(nacosServiceDiscovery.getServices()).map(services -> {
                ServiceCache.setServiceIds(services);
                return services;
            }).get();
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "get service name from nacos server failed. "
                    + "isFailureToleranceEnabled：{}", nacosRegisterConfig.isFailureToleranceEnabled()), e);
            return nacosRegisterConfig.isFailureToleranceEnabled() ? ServiceCache.getServiceIds()
                    : Collections.emptyList();
        }
    }
}
