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

package io.sermant.core.service.tracing.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Span Event
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
     * spanIdPrefix of next process
     */
    private transient String nextSpanIdPrefix;

    private String className;

    private String method;

    private transient SpanEvent parentSpan;

    /**
     * operation description of span
     */
    private String operationDescription;

    /**
     * type of span, such as: mysql、kafka、http
     */
    private String type;

    private long startTime;

    private long endTime;

    private boolean isError;

    private String errorInfo;

    /**
     * whether span is async
     */
    private boolean isAsync;

    /**
     * source information of node who invoke this span
     */
    private SourceInfo sourceInfo;

    /**
     * target information of node who invoke this span
     */
    private TargetInfo targetInfo;

    private Map<String, String> tags = new LinkedHashMap<>();

    /**
     * constructor without parameters
     */
    public SpanEvent() {
    }

    /**
     * Create ChildrenSpan from ParentSpan
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
     * add tag to span
     *
     * @param key key
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
