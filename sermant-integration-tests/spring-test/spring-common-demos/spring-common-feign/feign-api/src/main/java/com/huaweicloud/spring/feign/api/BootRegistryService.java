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

package com.huaweicloud.spring.feign.api;

import com.huaweicloud.spring.common.registry.common.RegistryConstants;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 用于测试springboot-registry插件的调用能力-feign组件
 *
 * @author zhouss
 * @since 2022-10-25
 */
@FeignClient(name = "bootRegistry", url = "http://" + RegistryConstants.TEST_DOMAIN + "/feign-provider")
public interface BootRegistryService {
    /**
     * feign调用
     *
     * @return ok
     */
    @RequestMapping("/feignRegistry")
    String feignRegistry();

    /**
     * feign调用
     *
     * @return ok
     */
    @RequestMapping(value = "/feignRegistryPost", method = RequestMethod.POST)
    String feignRegistryPost();
}
