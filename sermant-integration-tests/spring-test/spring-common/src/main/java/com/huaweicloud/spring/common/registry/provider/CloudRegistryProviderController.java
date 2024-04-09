/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.spring.common.registry.provider;

import com.huaweicloud.spring.common.registry.common.RegistryConstants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * SpringCloud注册调用测试
 *
 * @author zhouss
 * @since 2022-11-01
 */
@Controller
@ResponseBody
@RequestMapping(RegistryConstants.CLOUD_REGISTRY_REQUEST_PREFIX)
public class CloudRegistryProviderController {
    @Value("${server.port:8080}")
    private int port;

    /**
     * 测试Cloud注册调用
     *
     * @return ok
     */
    @RequestMapping(value = "testCloudRegistry", method = RequestMethod.GET)
    public String testCloudRegistry() {
        return String.valueOf(port);
    }
}
