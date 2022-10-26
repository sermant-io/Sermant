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

package com.huaweicloud.spring.feign.consumer.controller;

import com.huaweicloud.spring.common.registry.common.RegistryConstants;
import com.huaweicloud.spring.feign.api.BootRegistryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 注册控制器
 *
 * @author zhouss
 * @since 2022-10-25
 */
@Controller
@ResponseBody
@RequestMapping(RegistryConstants.REGISTRY_REQUEST_PREFIX)
public class BootRegistryController {
    @Autowired
    private BootRegistryService bootRegistryService;

    /**
     * 测试feign调用
     *
     * @return ok
     */
    @RequestMapping("feignRegistry")
    public String feignRegistry() {
        return bootRegistryService.feignRegistry();
    }

    /**
     * 测试feign调用
     *
     * @return ok
     */
    @RequestMapping("feignRegistryPost")
    public String feignRegistryPost() {
        return bootRegistryService.feignRegistryPost();
    }
}
