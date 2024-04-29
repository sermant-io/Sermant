/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.spring.rest.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * 利群实例摘除插件测试接口
 *
 * @author zhp
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/removal")
public class RemovalController {
    @Autowired
    @Qualifier("removalRestTemplate")
    RestTemplate restTemplate;

    @Value("${down.serviceName}")
    private String downServiceName;

    @Value("${config.domain:www.domain.com}")
    private String domain;

    @Value("${timeout}")
    private int timeout;

    /**
     * 获取端口
     *
     * @return 端口
     */
    @GetMapping("/boot/testRemoval")
    public String testRemoval() {
        RestTemplate template = new RestTemplate();
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) template.getRequestFactory();
        rf.setReadTimeout(timeout);
        return template.getForObject("http://" + domain + "/" + downServiceName + "/removal/testRemoval", String.class);
    }

    /**
     * 获取端口
     *
     * @return 端口
     */
    @GetMapping("/cloud/testRemoval")
    public String testRemovalByCloud() {
        return restTemplate.getForObject("http://" + downServiceName + "/removal/testRemoval", String.class);
    }
}