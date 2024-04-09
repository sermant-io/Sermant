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

package com.huaweicloud.config.nacos.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动类
 *
 * @author zhouss
 * @since 2022-07-14
 */
@SpringBootApplication(scanBasePackages = {
    "com.huaweicloud.config.nacos.demo",
    "com.huaweicloud.spring.common.config"
})
@EnableDiscoveryClient
@Controller
@RestController
public class NacosApplication {
    @Autowired
    private Environment environment;

    /**
     * 启动方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NacosApplication.class, args);
    }

    /**
     * 获取标签
     *
     * @return 标签
     */
    @RequestMapping("/labels")
    public Map<String, String> getLabels() {
        final HashMap<String, String> labels = new HashMap<>();
        labels.put("app", environment.getProperty("service.meta.application"));
        labels.put("environment", environment.getProperty("service.meta.environment"));
        return labels;
    }
}
