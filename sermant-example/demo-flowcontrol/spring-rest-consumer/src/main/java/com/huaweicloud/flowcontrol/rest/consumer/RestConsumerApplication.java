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

package com.huaweicloud.flowcontrol.rest.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 启动类
 *
 * @author zhouss
 * @since 2022-08-01
 */
@SpringBootApplication
public class RestConsumerApplication {
    /**
     * 启动方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RestConsumerApplication.class, args);
    }

    /**
     * 请求器
     *
     * @return rest template
     */
    @Bean
    @ConditionalOnMissingBean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
