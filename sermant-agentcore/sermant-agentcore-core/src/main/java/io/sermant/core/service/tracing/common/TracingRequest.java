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

/**
 * Parameters that are transmitted in when calling the link trace lifecycle
 *
 * @author luanwenfei
 * @since 2022-03-01
 */
public class TracingRequest {
    private String traceId;

    private String parentSpanId;

    /**
     * spanIdPrefix of current process
     */
    private String spanIdPrefix;

    private String className;

    private String method;

    /**
     * sourceInfo of SpanContext
     */
    private SourceInfo sourceInfo;

    private TargetInfo targetInfo;

    /**
     * Constructor
     *
     * @param traceId traceId
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

    /**
     * Constructor
     *
     * @param className className
     * @param method method
     */
    public TracingRequest(String className, String method) {
        this.className = className;
        this.method = method;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
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
}
