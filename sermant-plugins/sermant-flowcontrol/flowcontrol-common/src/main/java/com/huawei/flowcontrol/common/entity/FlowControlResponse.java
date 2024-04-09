/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.common.entity;

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
     * In response to the result, directly replace the interface return value
     */
    private Object result;

    /**
     * Whether to replace the actual response result, if true, replace
     */
    private boolean isReplaceResult;

    /**
     * the result after serialization
     */
    private String serializeResult;

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
     * @param result response result
     */
    public FlowControlResponse(String msg, int code, Object result) {
        this.msg = msg;
        this.code = code;
        this.result = result;
        this.isReplaceResult = true;
    }

    public String getSerializeResult() {
        return serializeResult;
    }

    public void setSerializeResult(String serializeResult) {
        this.serializeResult = serializeResult;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public Object getResult() {
        return result;
    }

    public boolean isReplaceResult() {
        return isReplaceResult;
    }
}
