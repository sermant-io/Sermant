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

package com.huawei.nacos.rest.consumer;

import com.huawei.nacos.rest.consumer.aspect.StatAop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * 启动类
 *
 * @author zhouss
 * @since 2022-06-16
 */
@SpringBootApplication
@RestController
@Import(value = {StatAop.class})
public class NacosRestConsumerApplication {
    @Autowired
    RestTemplate restTemplate;
    /**
     * 启动
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NacosRestConsumerApplication.class);
    }

    /**
     * 测试方法
     *
     * @param mode 请求模式
     * @return 结果
     */
    @GetMapping("/hello")
    public String hello(@RequestParam(required = false) String mode) {
        return restTemplate.getForObject("http://nacos-rest-provider/hello?mode=" + mode, String.class);
    }
}

