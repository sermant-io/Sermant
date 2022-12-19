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

package com.huaweicloud.integration.service;

import java.util.Map;

/**
 * 流控接口
 *
 * @author zhouss
 * @since 2022-09-15
 */
public interface FlowControlService {
    /**
     * 限流测试
     *
     * @return 结果
     */
    String rateLimiting();

    /**
     * 限流测试
     *
     * @return 结果
     */
    String rateLimitingPrefix();

    /**
     * 限流测试
     *
     * @return 结果
     */
    String rateLimitingSuffix();

    /**
     * 限流测试
     *
     * @return 结果
     */
    String rateLimitingContains();

    /**
     * 限流测试
     *
     * @return 结果
     */
    String rateLimitingWithApplication();

    /**
     * 限流测试
     *
     * @param attachments header
     * @return 结果
     */
    String rateLimitingWithHeader(Map<String, Object> attachments);

    /**
     * 熔断测试-慢调用
     *
     * @return 结果
     */
    String cirSlowInvoker();

    /**
     * 熔断测试-异常率
     *
     * @return 结果
     */
    String cirEx();

    /**
     * 实例隔离-慢调用
     *
     * @return 结果
     */
    String instanceSlowInvoker();

    /**
     * 实例隔离-异常率
     *
     * @return 结果
     */
    String instanceEx();

    /**
     * 错误注入（降级）-返回空
     *
     * @return 结果
     */
    String faultNull();

    /**
     * 错误注入（降级）-抛出异常
     *
     * @return 结果
     */
    String faultThrowEx();

    /**
     * 错误注入（降级）-延迟
     *
     * @return 结果
     */
    String faultDelay();

    /**
     * 隔离仓测试
     *
     * @return 结果
     */
    String bulkhead();

    /**
     * 重试测试
     *
     * @param invocationId 调用ID
     * @return 结果
     */
    String retry(String invocationId);

    /**
     * lb测试
     */
    void lb();
}
