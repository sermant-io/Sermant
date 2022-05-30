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

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ServiceComb ServerList实现, 针对Ribbon
 *
 * @author zhouss
 * @since 2022-05-19
 */
public class ServiceCombServiceList extends AbstractServerList<ServiceCombServer> {
    private IClientConfig clientConfig;

    private RegisterCenterService registerCenterService;

    @Override
    public void initWithNiwsConfig(IClientConfig config) {
        this.clientConfig = config;
    }

    @Override
    public List<ServiceCombServer> getInitialListOfServers() {
        return getUpdatedListOfServers();
    }

    @Override
    public List<ServiceCombServer> getUpdatedListOfServers() {
        final List<MicroServiceInstance> serverList = getRegisterCenterService()
                .getServerList(clientConfig.getClientName());
        return serverList.stream().map(ServiceCombServer::new).collect(Collectors.toList());
    }

    private RegisterCenterService getRegisterCenterService() {
        if (registerCenterService == null) {
            registerCenterService = PluginServiceManager.getPluginService(RegisterCenterService.class);
        }
        return registerCenterService;
    }
}
