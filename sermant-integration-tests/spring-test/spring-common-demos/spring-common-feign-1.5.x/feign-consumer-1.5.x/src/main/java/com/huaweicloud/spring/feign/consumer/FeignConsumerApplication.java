/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.spring.feign.consumer;

import com.huaweicloud.spring.common.flowcontrol.YamlSourceFactory;

import feign.codec.ErrorDecoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * 启动类
 *
 * @author zhouss
 * @since 2022-07-29
 */
@SpringBootApplication(scanBasePackages = {
    "com.huaweicloud.spring.feign.consumer.controller",
    "com.huaweicloud.spring.common.loadbalancer.feign"
})
@EnableFeignClients(basePackages = "com.huaweicloud.spring.feign.api")
@PropertySource(value = "classpath:rule.yaml", factory = YamlSourceFactory.class)
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
}
