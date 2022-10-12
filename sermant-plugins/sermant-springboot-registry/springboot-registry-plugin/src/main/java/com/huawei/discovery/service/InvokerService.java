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

package com.huawei.discovery.service;

import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.retry.RetryConfig;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

import java.util.Optional;
import java.util.function.Function;

/**
 * 方法调用, 该服务用于接管调用拦截点, 由该方法决定调用逻辑
 *
 * @author zhouss
 * @since 2022-09-28
 */
public interface InvokerService extends PluginService {
    /**
     * 方法调用
     *
     * @param invokeFunc 方法调用器, 需返回实际调用的结果
     * @param exFunc 异常封装器
     * @param serviceName 目标服务名
     * @return 最终响应结果
     */
    Optional<Object> invoke(Function<InvokerContext, Object> invokeFunc,
            Function<Exception, Object> exFunc, String serviceName);

    /**
     * 基于自定义重试器进行调用
     *
     * @param invokeFunc 方法调用器, 需返回实际调用的结果
     * @param exFunc 异常封装器
     * @param serviceName 目标服务名
     * @param retryConfig 自定义重试配置
     * @return 最终响应结果
     */
    Optional<Object> invoke(Function<InvokerContext, Object> invokeFunc,
            Function<Exception, Object> exFunc, String serviceName, RetryConfig retryConfig);
}
