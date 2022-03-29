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

package com.huawei.sermant.core.service.tracing.api;

import com.huawei.sermant.core.service.BaseService;
import com.huawei.sermant.core.service.tracing.common.SpanEvent;
import com.huawei.sermant.core.service.tracing.common.SpanEventContext;
import com.huawei.sermant.core.service.tracing.common.TracingRequest;

import java.util.Optional;

/**
 * 链路追踪服务接口
 *
 * @author luanwenfei
 * @since 2022-02-28
 */
public interface TracingService extends BaseService {
    /**
     * 用于线程内全局获取SpanEventContext
     *
     * @return Optional< SpanEventContext >
     */
    Optional<SpanEventContext> getContext();

    /**
     * 工作单元开始生命周期，用于非Provider和Consumer场景的工作单元
     *
     * @param tracingRequest 调用链路追踪生命周期时需要传入的参数
     * @return 构建好的SpanEvent数据
     */
    Optional<SpanEvent> onNormalSpanStart(TracingRequest tracingRequest);

    /**
     * 工作单元开始生命周期，用于Provider场景的工作单元
     *
     * @param tracingRequest 调用链路追踪生命周期时需要传入的参数
     * @param extractService 透传时，从载体提取数据的函数式接口
     * @param carrier 协议载体
     * @param <T> 泛型
     * @return 构建好的SpanEvent数据
     */
    <T> Optional<SpanEvent> onProviderSpanStart(TracingRequest tracingRequest, ExtractService<T> extractService,
        T carrier);

    /**
     * 工作单元开始生命周期，用于Consumer场景的工作单元
     *
     * @param tracingRequest 调用链路追踪生命周期时需要传入的参数
     * @param injectService 透传时，注入载体的函数式接口
     * @param carrier 协议载体
     * @param <T> 泛型
     * @return 构建好的SpanEvent数据
     */
    <T> Optional<SpanEvent> onConsumerSpanStart(TracingRequest tracingRequest, InjectService<T> injectService,
        T carrier);

    /**
     * 工作单元结束生命周期
     */
    void onSpanFinally();

    /**
     * 工作单元异常生命周期
     *
     * @param throwable throwable
     * @return 构建好的SpanEvent数据
     */
    Optional<SpanEvent> onSpanError(Throwable throwable);
}
