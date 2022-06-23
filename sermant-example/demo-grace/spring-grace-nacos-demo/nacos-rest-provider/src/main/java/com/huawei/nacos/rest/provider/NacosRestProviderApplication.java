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

package com.huawei.nacos.rest.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * 启动类
 *
 * @author zhouss
 * @since 2022-06-16
 */
@SpringBootApplication
@RestController
public class NacosRestProviderApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosRestProviderApplication.class);

    private static final long SLEEP_MS = 150L;

    private static final double RANDOM = 0.1d;

    @Value("${server.port}")
    int port;

    /**
     * RestTemplate
     *
     * @return RestTemplate
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 启动
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NacosRestProviderApplication.class);
    }

    /**
     * 请求方法
     *
     * @param mode 模式
     * @return RequestMapping
     * @throws Exception 请求异常抛出
     */
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(@RequestParam(required = false) String mode) throws Exception {
        Thread.sleep(SLEEP_MS);
        if ("retry".equals(mode)) {
            LOGGER.info("retry port:{} -- {}", port, LocalDateTime.now());
            if (Math.random() > RANDOM) {
                throw new Exception("need retry");
            }
        }
        return "Hello, I am nacos rest template provider, my port is " + port;
    }
}
