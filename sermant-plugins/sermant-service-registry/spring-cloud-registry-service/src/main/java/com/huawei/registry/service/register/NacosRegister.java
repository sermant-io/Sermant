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

package com.huawei.registry.service.register;

import com.huawei.registry.config.RegisterType;
import com.huawei.registry.service.client.NacosClient;

import java.util.List;

/**
 * nacos注册实现
 *
 * @author chengyouling
 * @since 2022-10-20
 */
public class NacosRegister implements Register {
    private NacosClient client;

    @Override
    public void start() {
        client = new NacosClient();
    }

    @Override
    public void stop() {
        client.deregister();
    }

    @Override
    public void register() {
        client.register();
    }

    @Override
    public List<NacosServiceInstance> getInstanceList(String serviceId) {
        return client.getInstances(serviceId);
    }

    @Override
    public List<String> getServices() {
        return client.getServices();
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.NACOS;
    }

    @Override
    public String getRegisterCenterStatus() {
        return client.getServerStatus();
    }

    @Override
    public String getInstanceStatus() {
        return client.getInstanceStatus();
    }

    @Override
    public void updateInstanceStatus(String status) {
        client.updateInstanceStatus(status);
    }
}
