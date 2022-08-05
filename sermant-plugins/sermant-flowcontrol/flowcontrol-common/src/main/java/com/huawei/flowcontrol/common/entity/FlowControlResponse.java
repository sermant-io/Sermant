/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
 * 流控响应
 *
 * @author zhouss
 * @since 2022-08-05
 */
public class FlowControlResponse {
    private final String msg;

    private final int code;

    /**
     * 响应结果, 直接替换接口返回值
     */
    private Object result;

    /**
     * 是否替代实际响应结果, 若为true则替换
     */
    private boolean isReplaceResult;

    /**
     * 序列化之后的结果
     */
    private String serializeResult;

    /**
     * 流控响应结果
     *
     * @param msg 提示信息
     * @param code 响应码
     */
    public FlowControlResponse(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    /**
     * 流控响应结果
     *
     * @param msg 提示信息
     * @param code 响应码
     * @param result 响应结果
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
