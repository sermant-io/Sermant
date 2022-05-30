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

import com.huawei.registry.auto.sc.ServiceCombServerIntrospector;
import com.huawei.registry.auto.sc.ServiceCombServiceList;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ServiceComb 针对Ribbon自动配置
 *
 * @author zhouss
 * @since 2022-05-19
 */
@Configuration
public class ServiceCombRibbonConfiguration {
    /**
     * ServerList注入
     *
     * @param clientConfig 客户端配置
     * @return ServerList
     */
    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> serverList(IClientConfig clientConfig) {
        final ServiceCombServiceList serviceCombServiceList = new ServiceCombServiceList();
        serviceCombServiceList.initWithNiwsConfig(clientConfig);
        return serviceCombServiceList;
    }

    /**
     * meta重写
     *
     * @return ServiceCombServerIntrospector
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceCombServerIntrospector serviceCombServerIntrospector() {
        return new ServiceCombServerIntrospector();
    }
}
