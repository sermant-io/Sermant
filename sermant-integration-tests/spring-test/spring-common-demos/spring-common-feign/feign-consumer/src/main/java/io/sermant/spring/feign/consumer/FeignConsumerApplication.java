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

package io.sermant.spring.feign.consumer;

import feign.codec.ErrorDecoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 启动类
 *
 * @author zhouss
 * @since 2022-07-29
 */
@SpringBootApplication(scanBasePackages = {
        "io.sermant.spring.feign.consumer.controller",
        "io.sermant.spring.common.loadbalancer.feign",
        "io.sermant.spring.feign.api.configuration"
})
@EnableFeignClients(basePackages = "io.sermant.spring.feign.api")
public class FeignConsumerApplication {
    /**
     * 启动类
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FeignConsumerApplication.class, args);
    }

    /**
     * 错误解析器
     *
     * @return ErrorDecoder
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * webclient，需要懒加载以避免spring cloud Finchley.x无法负载均衡的bug
     *
     * @param builder 构造器
     * @return webclient
     */
    @Bean
    @Lazy
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    /**
     * 构造器
     *
     * @return builder
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder builder() {
        return WebClient.builder();
    }
}
