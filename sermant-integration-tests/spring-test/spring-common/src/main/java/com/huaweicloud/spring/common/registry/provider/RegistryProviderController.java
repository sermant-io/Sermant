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

package com.huaweicloud.spring.common.registry.provider;

import com.huaweicloud.spring.common.registry.common.RegistryConstants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 注册测试提供接口
 *
 * @author zhouss
 * @since 2022-10-25
 */
@Controller
@ResponseBody
@RequestMapping(RegistryConstants.REGISTRY_REQUEST_PREFIX)
public class RegistryProviderController {
    private final String responsePrefix = "Rest test! My port is ";

    @Value("${server.port:8099}")
    private int port;

    @Value("${config.retry.sleepMs:2000}")
    private int sleepMs;

    /**
     * 测试rest调用
     *
     * @return ok
     */
    @RequestMapping("restRegistry")
    public String restRegistry() {
        return responsePrefix + port;
    }

    /**
     * 测试rest调用
     *
     * @return ok
     */
    @RequestMapping(value = "restRegistryPost", method = RequestMethod.POST)
    public String restRegistryPost() {
        return responsePrefix + port;
    }

    /**
     * get
     *
     * @return ok
     */
    @RequestMapping("get")
    public String get() {
        return responsePrefix + port;
    }

    /**
     * post
     *
     * @return ok
     */
    @RequestMapping(value = "post", method = RequestMethod.POST)
    public String post() {
        return responsePrefix + port;
    }

    /**
     * retry
     *
     * @throws InterruptedException 不会触发
     * @return ok
     */
    @RequestMapping("retry")
    public String retry() throws InterruptedException {
        Thread.sleep(sleepMs);
        return responsePrefix + port;
    }
}
