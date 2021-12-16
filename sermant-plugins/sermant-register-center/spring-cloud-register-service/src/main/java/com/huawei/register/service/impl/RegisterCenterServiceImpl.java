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

package com.huawei.register.service.impl;

import com.huawei.register.service.register.RegisterManager;
import com.huawei.register.services.RegisterCenterService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.netflix.loadbalancer.Server;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * 注册实现
 *
 * @author zhouss
 * @since 2021-12-16
 */
public class RegisterCenterServiceImpl implements RegisterCenterService {

    @Override
    public void start() {
        RegisterManager.INSTANCE.start();
    }

    @Override
    public void stop() {
        RegisterManager.INSTANCE.stop();
    }

    @Override
    public void register(Object rawRegistration) {
        RegisterManager.INSTANCE.register(rawRegistration);
    }

    @Override
    public void replaceServerList(Object target, BeforeResult beforeResult) {
        final List<Server> serverList = RegisterManager.INSTANCE.getServerList(target);
        if (serverList != null) {
            beforeResult.setResult(serverList);
        }
    }

    @Override
    public void replaceServerList(String serviceId, BeforeResult beforeResult) {
        final List<ServiceInstance> serviceInstances = RegisterManager.INSTANCE.getServerList(serviceId);
        if (serviceInstances != null) {
            beforeResult.setResult(serviceInstances);
        }
    }
}
