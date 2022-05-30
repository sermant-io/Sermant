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

package com.huawei.registry.auto.sc.reactive;

import com.huawei.registry.auto.sc.ServiceCombServiceInstance;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 支持SpringCloud高版本ReactiveClient
 *
 * @author zhouss
 * @since 2022-06-07
 */
public class ServiceCombReactiveDiscoveryClient implements ReactiveDiscoveryClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombReactiveDiscoveryClient.class);

    private RegisterCenterService registerCenterService;

    @Override
    public String description() {
        return "Spring Cloud ServiceComb Reactive Discovery Client";
    }

    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {
        return Flux.defer(() -> {
            final List<MicroServiceInstance> serverList = getRegisterCenterService().getServerList(serviceId);
            final List<ServiceInstance> result = new ArrayList<>(serverList.size());
            serverList.forEach(server -> result.add(new ServiceCombServiceInstance(server)));
            return Flux.fromIterable(result);
        }).onErrorResume(ex -> {
            LOGGER.error("Can not acquire service {} instances", serviceId, ex);
            return Flux.empty();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<String> getServices() {
        return Flux.defer(() -> Flux.fromIterable(getRegisterCenterService().getServices()))
                .onErrorResume(ex -> {
                    LOGGER.error("Can not acquire services list", ex);
                    return Flux.empty();
                }).subscribeOn(Schedulers.boundedElastic());
    }

    private RegisterCenterService getRegisterCenterService() {
        if (registerCenterService == null) {
            registerCenterService = PluginServiceManager.getPluginService(RegisterCenterService.class);
        }
        return registerCenterService;
    }
}
