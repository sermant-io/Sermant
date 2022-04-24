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

package com.huaweicloud.sermant.core.service.tracing.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Span实体数据
 *
 * @author luanwenfei
 * @since 2022-02-28
 */
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
     * 无参构造方法
     */
    public SpanEvent() {
    }

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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getSpanIdPrefix() {
        return spanIdPrefix;
    }

    public void setSpanIdPrefix(String spanIdPrefix) {
        this.spanIdPrefix = spanIdPrefix;
    }

    public String getNextSpanIdPrefix() {
        return nextSpanIdPrefix;
    }

    public void setNextSpanIdPrefix(String nextSpanIdPrefix) {
        this.nextSpanIdPrefix = nextSpanIdPrefix;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public SpanEvent getParentSpan() {
        return parentSpan;
    }

    public void setParentSpan(SpanEvent parentSpan) {
        this.parentSpan = parentSpan;
    }

    public String getOperationDescription() {
        return operationDescription;
    }

    public void setOperationDescription(String operationDescription) {
        this.operationDescription = operationDescription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public SourceInfo getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(SourceInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
