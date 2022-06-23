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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryHealthIndicatorEnabled;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.discovery.health.DiscoveryClientHealthIndicatorProperties;
import org.springframework.cloud.client.discovery.health.reactive.ReactiveDiscoveryClientHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reactive Client 自动配置
 *
 * @author zhouss
 * @since 2022-06-07
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnReactiveDiscoveryEnabled
@AutoConfigureAfter(name = { "org.springframework.cloud.client.discovery.composite.reactive"
        + ".ReactiveCompositeDiscoveryClientAutoConfiguration"})
@AutoConfigureBefore(name = "org.springframework.cloud.client.ReactiveCommonsClientAutoConfiguration")
public class ServiceCombReactiveConfiguration {

    /**
     * 自动配置reactiveClient
     *
     * @return ServiceCombReactiveDiscoveryClient
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceCombReactiveDiscoveryClient serviceCombReactiveClient() {
        return new ServiceCombReactiveDiscoveryClient();
    }

    /**
     * 注入健康检查
     *
     * @param client reactiveClient
     * @param properties 健康检查配置
     * @return ReactiveDiscoveryClientHealthIndicator
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.health.ReactiveHealthIndicator")
    @ConditionalOnDiscoveryHealthIndicatorEnabled
    public ReactiveDiscoveryClientHealthIndicator consulReactiveDiscoveryClientHealthIndicator(
            ServiceCombReactiveDiscoveryClient client, DiscoveryClientHealthIndicatorProperties properties) {
        return new ReactiveDiscoveryClientHealthIndicator(client, properties);
    }
}
