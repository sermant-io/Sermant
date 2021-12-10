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

package com.huawei.javamesh.core.lubanops.bootstrap.trace;

/**
 * tracing header constance.
 */
public enum Headers {

    TRACE_ID("lubanops-ntrace-id"),
    GTRACE_ID("lubanops-gtrace-id"),
    SPAN_ID("lubanops-nspan-id"),
    ENV_ID("lubanops-nenv-id"),
    RESPONSE_ENV_ID("lubanops-response-nenv-id"),
    SOURCE_EVENT_ID("lubanops-sevent-id"),
    CSE_CONTEXT("x-cse-context"),
    DUBBO_CONTEXT("x-dubbo-context"),
    DOMAIN_ID("lubanops-ndomain-id");

    public static final String CLASS_FULL_NAME = Headers.class.getName();

    private String value;

    Headers(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
