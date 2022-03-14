/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.huawei.sermant.core.service.tracing.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 调用链路追踪生命周期时需要传入的参数
 *
 * @author luanwenfei
 * @since 2022-03-01
 */
@Getter
@Setter
public class TracingRequest {
    private String traceId;

    private String parentSpanId;

    /**
     * 当前进程生成SpanId的前缀
     */
    private String spanIdPrefix;

    private String className;

    private String method;

    /**
     * SpanContext的来源信息
     */
    private SourceInfo sourceInfo;

    private TargetInfo targetInfo;

    /**
     * 构造函数
     *
     * @param  traceId traceId
     * @param parentSpanId parentSpanId
     * @param spanIdPrefix spanIdPrefix
     * @param className className
     * @param method method
     */
    public TracingRequest(String traceId, String parentSpanId, String spanIdPrefix, String className, String method) {
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.spanIdPrefix = spanIdPrefix;
        this.className = className;
        this.method = method;
    }
}
