/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.auto.sc;

import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ServiceComb服务实例查询
 *
 * @author zhouss
 * @since 2022-05-18
 */
public class ServiceCombDiscoveryClient implements DiscoveryClient {
    private RegisterCenterService registerCenterService;

    @Override
    public String description() {
        return "ServiceComb Discovery Client";
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        final List<MicroServiceInstance> serverList = getRegisterCenterService().getServerList(serviceId);
        return serverList.stream().map(ServiceCombServiceInstance::new).collect(Collectors.toList());
    }

    @Override
    public List<String> getServices() {
        return getRegisterCenterService().getServices();
    }

    private RegisterCenterService getRegisterCenterService() {
        if (registerCenterService == null) {
            registerCenterService = PluginServiceManager.getPluginService(RegisterCenterService.class);
        }
        return registerCenterService;
    }
}
