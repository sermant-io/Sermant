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

package com.huawei.registry.auto.sc.configuration;

import com.huawei.registry.auto.sc.ServiceCombAutoRegistration;
import com.huawei.registry.auto.sc.ServiceCombDiscoveryClient;
import com.huawei.registry.auto.sc.ServiceCombHealthIndicator;
import com.huawei.registry.auto.sc.ServiceCombRegistration;
import com.huawei.registry.auto.sc.ServiceCombRegistry;
import com.huawei.registry.auto.sc.ServiceInstanceHolder;
import com.huawei.registry.config.RegisterConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

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
 * ServiceComb自动注册配置
 *
 * @author zhouss
 * @since 2022-05-18
 */
@Configuration
@EnableConfigurationProperties
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)
@AutoConfigureAfter({AutoServiceRegistrationConfiguration.class,
    AutoServiceRegistrationAutoConfiguration.class})
public class ServiceCombAutoDiscoveryConfiguration {
    /**
     * 服务查询注入
     *
     * @return 服务查询client
     */
    @Bean
    public DiscoveryClient discoveryClient() {
        return new ServiceCombDiscoveryClient();
    }

    /**
     * 自动注册注入
     *
     * @param autoServiceRegistrationProperties 自动注册配置
     * @return 自动注册
     */
    @Bean
    public ServiceCombAutoRegistration serviceCombAutoRegistration(
            AutoServiceRegistrationProperties autoServiceRegistrationProperties) {
        return new ServiceCombAutoRegistration(new ServiceCombRegistry(),
                autoServiceRegistrationProperties, new ServiceCombRegistration(new ServiceInstanceHolder()),
                PluginConfigManager.getPluginConfig(RegisterConfig.class));
    }

    /**
     * 注入健康检查
     *
     * @return ServiceCombHealthIndicator
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.health.Health")
    public ServiceCombHealthIndicator serviceCombHealthIndicator() {
        return new ServiceCombHealthIndicator();
    }
}
