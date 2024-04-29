/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.spring.feign.consumer.controller;

import io.sermant.spring.common.FeignConstants;
import io.sermant.spring.feign.api.RemovalBootService;
import io.sermant.spring.feign.api.RemovalService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 流控测试
 *
 * @author zhp
 * @since 2023-03-16
 */
@Controller
@ResponseBody
@RequestMapping("removal")
public class RemovalController {
    @Autowired
    @Qualifier(FeignConstants.REMOVAL_SERVICE_BEAN_NAME)
    private RemovalService removalService;

    @Autowired
    private RemovalBootService removalBootService;

    /**
     * 离群实例摘除调用
     *
     * @return 调用结果
     */
    @RequestMapping("/cloud/testRemoval")
    public String removal() {
        return removalService.testRemoval();
    }

    /**
     * 离群实例摘除调用
     *
     * @return 调用结果
     */
    @RequestMapping("/boot/testRemoval")
    public String testBootRemoval() {
        return removalBootService.testRemoval();
    }
}
