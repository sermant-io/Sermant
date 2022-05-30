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

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.context.RegisterContext;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * ServiceComb自动配置
 *
 * @author zhouss
 * @since 2022-05-18
 */
public class ServiceCombAutoRegistration extends AbstractAutoServiceRegistration<ServiceCombRegistration> {
    private final RegisterConfig registerConfig;

    private final ServiceCombRegistration serviceCombRegistration;

    /**
     * 构造函数
     *
     * @param serviceRegistry 注册
     * @param properties 配置
     * @param serviceCombRegistration 注册信息
     * @param registerConfig 注册配置
     */
    public ServiceCombAutoRegistration(
            ServiceRegistry<ServiceCombRegistration> serviceRegistry,
            AutoServiceRegistrationProperties properties,
            ServiceCombRegistration serviceCombRegistration,
            RegisterConfig registerConfig) {
        super(serviceRegistry, properties);
        this.serviceCombRegistration = serviceCombRegistration;
        this.registerConfig = registerConfig;
    }

    @Override
    protected Object getConfiguration() {
        return registerConfig;
    }

    @Override
    protected boolean isEnabled() {
        return registerConfig.isEnableSpringRegister();
    }

    @Override
    protected ServiceCombRegistration getRegistration() {
        return serviceCombRegistration;
    }

    @Override
    protected ServiceCombRegistration getManagementRegistration() {
        return serviceCombRegistration;
    }

    /**
     * 获取端口 低版本需要实现的方法
     *
     * @return 端口
     */
    protected int getConfiguredPort() {
        return RegisterContext.INSTANCE.getClientInfo().getPort();
    }

    /**
     * 设置端口
     *
     * @param port 低版本需实现的方法
     */
    protected void setConfiguredPort(int port) {
    }
}
