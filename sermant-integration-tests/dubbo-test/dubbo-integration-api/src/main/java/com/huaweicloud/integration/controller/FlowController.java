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

import com.huaweicloud.integration.configuration.LbCache;
import com.huaweicloud.integration.service.FlowControlService;
import com.huaweicloud.integration.service.FlowControlVersionService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import javax.annotation.Resource;

/**
 * dubbo 流控测试
 *
 * @author zhouss
 * @since 2022-09-15
 */
@RestController
@RequestMapping("/consumer/flow")
public class FlowController {
    @Resource(name = "flowControlService")
    private FlowControlService flowControlService;

    @Resource(name = "flowControlVersionService")
    private FlowControlVersionService flowControlVersionService;

    /**
     * 测试限流
     *
     * @return 测试信息
     */
    @GetMapping("/rateLimiting")
    public String rateLimiting() {
        return flowControlService.rateLimiting();
    }

    /**
     * 测试限流
     *
     * @return 测试信息
     */
    @GetMapping("/rateLimitingPrefix")
    public String rateLimitingPrefix() {
        return flowControlService.rateLimitingPrefix();
    }

    /**
     * 测试限流
     *
     * @return 测试信息
     */
    @GetMapping("/rateLimitingSuffix")
    public String rateLimitingSuffix() {
        return flowControlService.rateLimitingSuffix();
    }

    /**
     * 测试限流
     *
     * @return 测试信息
     */
    @GetMapping("/rateLimitingContains")
    public String rateLimitingContains() {
        return flowControlService.rateLimitingContains();
    }

    /**
     * 测试限流
     *
     * @return 测试信息
     */
    @GetMapping("/rateLimitingWithApplication")
    public String rateLimitingWithApplication() {
        return flowControlService.rateLimitingWithApplication();
    }

    /**
     * 流控版本测试
     *
     * @return 测试信息
     */
    @GetMapping("/rateLimitingWithVersion")
    public String rateLimitingWithVersion() {
        return flowControlVersionService.rateLimitingWithVersion();
    }

    /**
     * 测试熔断-慢调用
     *
     * @return 测试信息
     */
    @GetMapping("/cirSlowInvoker")
    public String cirSlowInvoker() {
        return flowControlService.cirSlowInvoker();
    }

    /**
     * 测试熔断-异常
     *
     * @return 测试信息
     */
    @GetMapping("/cirEx")
    public String cirEx() {
        return flowControlService.cirEx();
    }

    /**
     * 实例隔离-慢调用
     *
     * @return 测试信息
     */
    @GetMapping("/instanceSlowInvoker")
    public String instanceSlowInvoker() {
        return flowControlService.instanceSlowInvoker();
    }

    /**
     * 实例隔离-异常
     *
     * @return 测试信息
     */
    @GetMapping("/instanceEx")
    public String instanceEx() {
        return flowControlService.instanceEx();
    }

    /**
     * 错误注入（降级）-返回空
     *
     * @return 测试信息
     */
    @GetMapping("/faultNull")
    public String faultNull() {
        return flowControlService.faultNull();
    }

    /**
     * 错误注入（降级）-抛异常
     *
     * @return 测试信息
     */
    @GetMapping("/faultThrowEx")
    public String faultThrowEx() {
        return flowControlService.faultThrowEx();
    }

    /**
     * 错误注入（降级）-延迟
     *
     * @return 测试信息
     */
    @GetMapping("/faultDelay")
    public String faultDelay() {
        return flowControlService.faultDelay();
    }

    /**
     * 隔离仓测试
     *
     * @return 测试信息
     */
    @GetMapping("/bulkhead")
    public String bulkhead() {
        return flowControlService.bulkhead();
    }

    /**
     * 重试测试
     *
     * @return 测试信息
     */
    @GetMapping("/retry")
    public String retry() {
        return flowControlService.retry(UUID.randomUUID().toString());
    }

    /**
     * 负载均衡
     *
     * @return 测试信息
     */
    @GetMapping("/lb")
    public String lb() {
        flowControlService.lb();
        final String lb = LbCache.INSTANCE.getLb();
        LbCache.INSTANCE.setLb(null);
        return lb;
    }
}
