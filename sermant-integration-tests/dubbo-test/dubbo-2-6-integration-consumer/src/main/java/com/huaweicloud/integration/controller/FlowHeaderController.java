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

package com.huaweicloud.integration.controller;

import com.huaweicloud.integration.service.FlowControlService;

import com.alibaba.dubbo.rpc.RpcContext;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

import javax.annotation.Resource;

/**
 * 流控lb测试
 *
 * @author zhouss
 * @since 2022-09-19
 */
@RestController
@RequestMapping("/consumer/flow")
public class FlowHeaderController {
    @Resource(name = "flowControlService")
    private FlowControlService flowControlService;

    /**
     * 测试限流
     *
     * @param key header键
     * @param value header值
     * @return 测试信息
     */
    @GetMapping("/rateLimitingWithHeader")
    public String rateLimitingWithHeader(@RequestParam(name = "key") String key, @RequestParam("value") String value) {
        try {
            RpcContext.getContext().setAttachment(key, value);
            return flowControlService.rateLimitingWithHeader(Collections.singletonMap(key, value));
        } finally {
            RpcContext.getContext().remove(key);
        }
    }
}
