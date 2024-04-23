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

package io.sermant.registry.auto.sc.configuration;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.registry.auto.sc.ServiceCombAutoRegistration;
import io.sermant.registry.auto.sc.ServiceCombDiscoveryClient;
import io.sermant.registry.auto.sc.ServiceCombHealthIndicator;
import io.sermant.registry.auto.sc.ServiceCombRegistration;
import io.sermant.registry.auto.sc.ServiceCombRegistry;
import io.sermant.registry.auto.sc.ServiceInstanceHolder;
import io.sermant.registry.config.RegisterConfig;
import io.sermant.registry.config.RegistrationProperties;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ServiceComb Automatic registration configuration
 *
 * @author zhouss
 * @since 2022-05-18
 */
@Configuration
@EnableConfigurationProperties
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)
@AutoConfigureAfter({AutoServiceRegistrationConfiguration.class, AutoServiceRegistrationAutoConfiguration.class})
@ConditionalOnClass(name = "org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration")
public class ServiceCombAutoDiscoveryConfiguration {
    /**
     * Service query injection
     *
     * @return Service query client
     */
    @Bean
    public DiscoveryClient discoveryClient() {
        return new ServiceCombDiscoveryClient();
    }

    /**
     * Automatic registration injection
     *
     * @param autoServiceRegistrationProperties Automatic registration configuration
     * @param registrationProperties Registration Configuration
     * @return Automatic registration
     */
    @Bean
    public ServiceCombAutoRegistration serviceCombAutoRegistration(
            AutoServiceRegistrationProperties autoServiceRegistrationProperties,
            RegistrationProperties registrationProperties) {
        return new ServiceCombAutoRegistration(
                new ServiceCombRegistry(),
                autoServiceRegistrationProperties,
                new ServiceCombRegistration(new ServiceInstanceHolder(registrationProperties)),
                PluginConfigManager.getPluginConfig(RegisterConfig.class));
    }

    /**
     * Inject health checks
     *
     * @return ServiceCombHealthIndicator
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.health.Health")
    public ServiceCombHealthIndicator serviceCombHealthIndicator() {
        return new ServiceCombHealthIndicator();
    }
}
