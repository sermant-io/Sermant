/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.integration;

import com.huaweicloud.integration.configuration.FlowControlExceptionHandler;
import com.huaweicloud.integration.configuration.FlowRuleConfiguration;
import com.huaweicloud.integration.controller.ProviderController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.client.RestTemplate;

/**
 * 启动类
 *
 * @author provenceee
 * @since 2022-04-28
 */
@SpringBootApplication
@ImportResource({"classpath:dubbo/consumer.xml"})
@Import({FlowRuleConfiguration.class, FlowControlExceptionHandler.class})
@EnableFeignClients(basePackages = "com.huaweicloud.integration.client")
@ComponentScan(excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = ProviderController.class))
public class ConsumerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerApplication.class);

    /**
     * spring启动类
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        LOGGER.info("====================start=======================");
        SpringApplication.run(ConsumerApplication.class);
        LOGGER.info("=====================end========================");
    }

    /**
     * restTemplate
     *
     * @return restTemplate
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
