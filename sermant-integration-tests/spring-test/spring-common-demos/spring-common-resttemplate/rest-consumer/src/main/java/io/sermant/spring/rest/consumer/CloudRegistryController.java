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

package io.sermant.spring.rest.consumer;

import io.sermant.spring.common.registry.common.RegistryConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

/**
 * SpringCloud调用
 *
 * @author zhouss
 * @since 2022-11-01
 */
@Controller
@ResponseBody
@RequestMapping(RegistryConstants.CLOUD_REGISTRY_REQUEST_PREFIX)
public class CloudRegistryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudRegistryController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${config.downStream:rest-provider}")
    private String downStream;

    /**
     * 测试cloud调用
     *
     * @return ok
     */
    @RequestMapping(value = "/testCloudRegistry", method = RequestMethod.GET)
    public String testCloudRegistry() {
        LOGGER.info("============instances============");

        // 直接打印可看出有哪些实例, 以及实例是哪个注册中心的
        LOGGER.info(discoveryClient.getInstances("feign-provider").toString());
        return restTemplate.getForObject(String.format(Locale.ENGLISH, "http://%s/cloudRegistry/testCloudRegistry",
                downStream), String.class);
    }
}
