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
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Span实体数据
 *
 * @author luanwenfei
 * @since 2022-02-28
 */
@Getter
@Setter
@NoArgsConstructor
public class SpanEvent {
    private String traceId;

    private String spanId;

    private String parentSpanId;

    private transient String spanIdPrefix;

    /**
     * 下一进程的spanIdPrefix
     */
    private transient String nextSpanIdPrefix;

    private String className;

    private String method;

    private transient SpanEvent parentSpan;

    /**
     * Span所标识工作单元执行的操作描述
     */
    private String operationDescription;

    /**
     * Span所标识工作单元的类型 mysql、kafka、http等
     */
    private String type;

    private long startTime;

    private long endTime;

    private boolean isError;

    private String errorInfo;

    /**
     * Span所标识工作单元是否为异步
     */
    private boolean isAsync;

    /**
     * 调用当前Span所标识工作单元的节点信息
     */
    private SourceInfo sourceInfo;

    /**
     * Span所标识工作单元调用的目的地的节点信息
     */
    private TargetInfo targetInfo;

    private Map<String, String> tags = new LinkedHashMap<>();

    /**
     * 通过ParentSpan创建ChildrenSpan
     *
     * @param spanEvent ParentSpan
     */
    public SpanEvent(SpanEvent spanEvent) {
        this.parentSpan = spanEvent;
        this.traceId = spanEvent.getTraceId();
        this.parentSpanId = spanEvent.getSpanId();
        this.spanIdPrefix = spanEvent.getSpanIdPrefix();
    }

    /**
     * 为Span添加标签
     *
     * @param key   key
     * @param value value
     */
    public void addTag(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        this.tags.put(key, value);
    }
}
