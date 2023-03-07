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

package com.huaweicloud.integration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * feign client
 *
 * @author provenceee
 * @since 2022-07-26
 */
@FeignClient(name = "dubbo-integration-provider")
public interface ProviderClient {
    /**
     * 测试接口
     *
     * @return 测试信息
     */
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    String hello();

    /**
     * 测试接口
     *
     * @return 测试信息
     */
    @RequestMapping(value = "/lane", method = RequestMethod.GET)
    Map<String, Object> getLane();
}
