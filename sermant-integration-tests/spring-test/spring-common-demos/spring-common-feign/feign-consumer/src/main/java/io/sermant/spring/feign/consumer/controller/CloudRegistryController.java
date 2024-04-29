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

package io.sermant.spring.feign.consumer.controller;

import io.sermant.spring.common.registry.common.RegistryConstants;
import io.sermant.spring.feign.api.FeignService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * SpringCloud 注册测试
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
    private FeignService feignService;

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * 测试cloud注册调用
     *
     * @return ok
     */
    @RequestMapping("testCloudRegistry")
    public String testCloudRegistry() {
        LOGGER.info("============instances============");

        // 直接打印可看出有哪些实例, 以及实例是哪个注册中心的
        LOGGER.info(discoveryClient.getInstances("feign-provider").toString());
        return feignService.testCloudRegistry();
    }
}
