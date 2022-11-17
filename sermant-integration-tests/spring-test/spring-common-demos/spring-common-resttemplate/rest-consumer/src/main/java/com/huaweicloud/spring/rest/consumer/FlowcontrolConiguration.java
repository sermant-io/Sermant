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

package com.huaweicloud.spring.rest.consumer;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 流控配置类
 *
 * @author zhouss
 * @since 2022-07-28
 */
@Configuration
public class FlowcontrolConiguration {
    /**
     * 注入请求器
     *
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 注入请求器
     *
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean("routerRestTemplate")
    public RestTemplate routerRestTemplate() {
        return new RestTemplate();
    }

    /**
     * 注入请求器
     *
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean("gracefulRestTemplate")
    public RestTemplate gracefulRestTemplate() {
        return new RestTemplate();
    }
}
