/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.service.register;

import com.huawei.registry.service.client.ScClient;

import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SC注册实现
 *
 * @author zhouss
 * @since 2021-12-17
 */
public class ScRegister implements Register {
    private ScClient client;

    @Override
    public void start() {
        client = new ScClient();
        client.init();
    }

    @Override
    public void stop() {
    }

    @Override
    public void register() {
        client.register();
    }

    @Override
    public List<ServicecombServiceInstance> getInstanceList(String serviceId) {
        final List<MicroserviceInstance> microserviceInstances = getScInstances(serviceId);
        final List<ServicecombServiceInstance> serviceInstances = new ArrayList<>();
        for (final MicroserviceInstance microserviceInstance : microserviceInstances) {
            serviceInstances.add(new ServicecombServiceInstance(microserviceInstance));
        }
        return serviceInstances;
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.SERVICE_COMB;
    }

    private List<MicroserviceInstance> getScInstances(String serviceName) {
        if (serviceName == null) {
            return Collections.emptyList();
        }
        final List<MicroserviceInstance> microserviceInstances = client.queryInstancesByServiceId(serviceName);
        if (microserviceInstances == null) {
            return Collections.emptyList();
        }
        microserviceInstances.removeIf(next -> next.getStatus() != MicroserviceInstanceStatus.UP);
        return microserviceInstances;
    }
}
