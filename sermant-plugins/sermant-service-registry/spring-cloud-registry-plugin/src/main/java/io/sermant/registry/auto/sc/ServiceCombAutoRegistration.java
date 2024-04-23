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

package io.sermant.registry.auto.sc;

import io.sermant.registry.config.RegisterConfig;
import io.sermant.registry.context.RegisterContext;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * ServiceComb automatic configuration
 *
 * @author zhouss
 * @since 2022-05-18
 */
public class ServiceCombAutoRegistration extends AbstractAutoServiceRegistration<ServiceCombRegistration> {
    private final RegisterConfig registerConfig;

    private final ServiceCombRegistration serviceCombRegistration;

    /**
     * Constructor
     *
     * @param serviceRegistry register
     * @param properties configuration
     * @param serviceCombRegistration Registration Information
     * @param registerConfig Registration configuration
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
     * Get Port Methods that need to be implemented in earlier versions
     *
     * @return Port
     */
    protected int getConfiguredPort() {
        return RegisterContext.INSTANCE.getClientInfo().getPort();
    }

    /**
     * Set the port
     *
     * @param port Methods to be implemented in earlier versions
     */
    protected void setConfiguredPort(int port) {
    }
}
