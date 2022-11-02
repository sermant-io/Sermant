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
import com.huawei.registry.service.register.NacosServiceInstance;
import com.huawei.registry.service.register.NacosServiceManager;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;


/**
 * nacos注册实现客户端
 *
 * @author chengyouling
 * @since 2022-10-20
 */
public class NacosServiceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final NacosRegisterConfig nacosRegisterConfig;

    private final NacosServiceManager nacosServiceManager;

    private String myselfServiceId;

    /**
     * 构造方法
     *
     * @param nacosServiceManager
     */
    public NacosServiceDiscovery(NacosServiceManager nacosServiceManager) {
        this.nacosServiceManager = nacosServiceManager;
        nacosRegisterConfig = PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
    }


    /**
     * 设置自身服务id
     *
     * @param serviceId
     */
    public void updateMyselfServiceId(String serviceId) {
        this.myselfServiceId = serviceId;
    }

    /**
     * 获取对应服务名微服务实例信息
     *
     * @param serviceId
     * @return 服务信息
     * @throws NacosException
     */
    public List<NacosServiceInstance> getInstances(String serviceId) throws NacosException {
        String group = nacosRegisterConfig.getGroup();
        NamingService namingService = nacosServiceManager.getNamingService();
        List<Instance> instances = namingService.selectInstances(serviceId, group, true);
        return hostToServiceInstanceList(instances, serviceId);
    }

    /**
     * 实例集合信息转换
     *
     * @param instances
     * @param serviceId
     * @return 转换后实例集合信息
     */
    public List<NacosServiceInstance> hostToServiceInstanceList(List<Instance> instances, String serviceId) {
        List<NacosServiceInstance> result = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            Optional<NacosServiceInstance> optional = hostToServiceInstance(instance, serviceId);
            if (optional.isPresent()) {
                result.add(optional.get());
            }
        }
        return result;
    }

    /**
     * 实例信息转换
     *
     * @param instance
     * @param serviceId
     * @return 转换后实例信息
     */
    public Optional<NacosServiceInstance> hostToServiceInstance(Instance instance, String serviceId) {
        if (instance == null || !instance.isEnabled() || !instance.isHealthy()) {
            return Optional.empty();
        }
        NacosServiceInstance nacosServiceInstance = new NacosServiceInstance();
        nacosServiceInstance.setHost(instance.getIp());
        nacosServiceInstance.setPort(instance.getPort());
        nacosServiceInstance.setServiceId(serviceId);
        nacosServiceInstance.setInstanceId(instance.getInstanceId());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("nacos.instanceId", instance.getInstanceId());
        metadata.put("nacos.weight", instance.getWeight() + "");
        metadata.put("nacos.healthy", instance.isHealthy() + "");
        metadata.put("nacos.cluster", instance.getClusterName() + "");
        if (instance.getMetadata() != null) {
            metadata.putAll(instance.getMetadata());
        }
        metadata.put("nacos.ephemeral", String.valueOf(instance.isEphemeral()));
        nacosServiceInstance.setMetadata(metadata);

        if (metadata.containsKey("secure")) {
            boolean secure = Boolean.parseBoolean(metadata.get("secure"));
            nacosServiceInstance.setSecure(secure);
        }
        return Optional.of(nacosServiceInstance);
    }

    /**
     * 获取所有服务名称
     *
     * @return 服务名称集合
     * @throws NacosException
     */
    public List<String> getServices() throws NacosException {
        String group = nacosRegisterConfig.getGroup();
        NamingService namingService = nacosServiceManager.getNamingService();
        ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, group);
        return services.getData();
    }
}
