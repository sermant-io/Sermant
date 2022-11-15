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

package com.huaweicloud.spring.rest.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * 测试接口
 *
 * @author provenceee
 * @since 2022-11-02
 */
@RestController
@RequestMapping("/router")
public class RouterController {
    @Value("${down.serviceName}")
    private String downServiceName;

    @Value("${config.domain:www.domain.com}")
    private String domain;

    @Autowired
    @Qualifier("routerRestTemplate")
    private RestTemplate restTemplate;

    private final RestTemplate localRestTemplate = new RestTemplate();

    /**
     * 获取区域
     *
     * @param exit 是否退出
     * @return 区域
     */
    @GetMapping("/boot/getMetadata")
    public String getMetadataByBoot(boolean exit) {
        return localRestTemplate
            .getForObject("http://" + domain + "/" + downServiceName + "/router/metadata?exit=" + exit, String.class);
    }

    /**
     * 获取区域
     *
     * @param exit 是否退出
     * @return 区域
     */
    @GetMapping("/cloud/getMetadata")
    public String getMetadataByCloud(boolean exit) {
        return restTemplate.getForObject("http://" + downServiceName + "/router/metadata?exit=" + exit, String.class);
    }
}