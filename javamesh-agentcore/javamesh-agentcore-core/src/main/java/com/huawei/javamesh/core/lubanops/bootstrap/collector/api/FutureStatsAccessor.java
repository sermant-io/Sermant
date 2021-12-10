/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.javamesh.core.lubanops.bootstrap.collector.api;


import com.huawei.javamesh.core.lubanops.bootstrap.api.SpanEventAccessor;
import com.huawei.javamesh.core.lubanops.bootstrap.trace.SpanEvent;

/**
 * @author
 */
public class FutureStatsAccessor implements SpanEventAccessor {
    private String serviceName;

    private String method;

    private String envId;

    private long startTime;

    private SpanEvent spanEvent;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceUniqueName) {
        this.serviceName = serviceUniqueName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public SpanEvent getSpanEvent() {
        return spanEvent;
    }

    @Override
    public void setSpanEvent(SpanEvent spanEvent) {
        this.spanEvent = spanEvent;
    }
}
