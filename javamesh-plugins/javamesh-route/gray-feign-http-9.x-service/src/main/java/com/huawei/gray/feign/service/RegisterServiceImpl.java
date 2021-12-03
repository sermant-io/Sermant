/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.gray.feign.service;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.gray.feign.context.CurrentInstance;

import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;
import com.netflix.appinfo.InstanceInfo;

import org.apache.servicecomb.registry.api.registry.Microservice;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;

import java.lang.reflect.Method;

/**
 * 注册通用的service
 *
 * @author pengyuyi
 * @date 2021/11/26
 */
public class RegisterServiceImpl implements RegisterService {
    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        final Object argument = arguments[0];
        if (argument instanceof InstanceInfo) {
            InstanceInfo instanceInfo = (InstanceInfo) argument;
            // 存放当前服务实例, 初始化标签观察者
            CurrentInstance.newInstance(instanceInfo.getVIPAddress(), instanceInfo.getIPAddr(), instanceInfo.getPort());
        } else if (argument instanceof ZookeeperRegistration) {
            ZookeeperRegistration instanceInfo = (ZookeeperRegistration) argument;
            // 存放当前服务实例, 仅初始化一次
            CurrentInstance.newInstance(instanceInfo.getServiceId(), instanceInfo.getHost(), instanceInfo.getPort());
        } else if (argument instanceof Registration) {
            Registration instanceInfo = (Registration) argument;
            // 存放当前服务实例, 仅初始化一次
            CurrentInstance.newInstance(instanceInfo.getServiceId(), instanceInfo.getHost(), instanceInfo.getPort());
        }
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (result instanceof Microservice) {
            Microservice microservice = (Microservice) result;
            CurrentInstance.newInstance(microservice.getServiceName(), microservice.getInstance().getHostName(), -1);
        } else if (arguments[0] instanceof ServiceCombRegistration) {
            ServiceCombRegistration serviceCombRegistration = (ServiceCombRegistration) arguments[0];
            CurrentInstance.newInstance(serviceCombRegistration.getServiceId(), serviceCombRegistration.getHost(),
                    serviceCombRegistration.getPort());
        }
    }
}