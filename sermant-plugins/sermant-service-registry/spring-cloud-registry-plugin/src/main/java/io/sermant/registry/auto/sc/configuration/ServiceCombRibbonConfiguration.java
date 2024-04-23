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

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;

import io.sermant.registry.auto.sc.ServiceCombServerIntrospector;
import io.sermant.registry.auto.sc.ServiceCombServiceList;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ServiceComb Automatic configuration for ribbons
 *
 * @author zhouss
 * @since 2022-05-19
 */
@Configuration
public class ServiceCombRibbonConfiguration {
    /**
     * ServerList injection
     *
     * @param clientConfig Client configuration
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
     * meta rewrite
     *
     * @return ServiceCombServerIntrospector
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceCombServerIntrospector serviceCombServerIntrospector() {
        return new ServiceCombServerIntrospector();
    }
}
