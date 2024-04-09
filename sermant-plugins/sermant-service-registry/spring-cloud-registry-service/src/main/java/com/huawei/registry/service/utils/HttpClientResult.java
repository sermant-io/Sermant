/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.service.utils;

/**
 * Description: Encapsulates the HTTP client response
 *
 * @author provenceee
 * @since 2022-05-26
 */
public class HttpClientResult {
    /**
     * Response status code
     */
    private final int code;

    /**
     * Response data
     */
    private final String msg;

    /**
     * Constructor
     *
     * @param code Response code
     */
    public HttpClientResult(int code) {
        this(code, null);
    }

    /**
     * Constructor
     *
     * @param code Response code
     * @param msg Response message
     */
    public HttpClientResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}