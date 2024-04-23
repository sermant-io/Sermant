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

package io.sermant.integration.controller;

import io.sermant.integration.service.RemovalService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 离群实例摘除测试接口
 *
 * @author zhp
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/removal")
public class RemovalController {
    @Resource(name = "removalService")
    private RemovalService removalService;

    /**
     * 测试离群实例摘除接口接口
     *
     * @return 测试信息
     */
    @GetMapping("/testReq")
    public String testReq() {
        return removalService.getPort();
    }
}