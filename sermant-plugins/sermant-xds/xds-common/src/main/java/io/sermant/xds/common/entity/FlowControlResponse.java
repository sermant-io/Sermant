/*
 * Copyright (C) 2022-2025 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.xds.common.entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * flow control response
 *
 * @author zhouss
 * @since 2022-08-05
 */
public class FlowControlResponse {
    private final String msg;

    private final int code;

    /**
     * response
     */
    private Map<String, List<String>> headers;

    /**
     * flow control response results
     *
     * @param msg prompt message
     * @param code response code
     */
    public FlowControlResponse(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    /**
     * flow control response results
     *
     * @param msg prompt message
     * @param code response code
     * @param headers response headers
     */
    public FlowControlResponse(String msg, int code, Map<String, List<String>> headers) {
        this.msg = msg;
        this.code = code;
        this.headers = headers;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public Map<String, List<String>> getHeaders() {
        return headers == null ? Collections.emptyMap() : headers;
    }
}
