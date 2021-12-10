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

package com.huawei.javamesh.backend.service.dynamicconfig.kie.client.http;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * httpclient响应结果
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class HttpResult {
    /**
     * 错误请求响应码
     */
    public static final int ERROR_CODE = -1;

    /**
     * 可接受编号
     * SC_OK : 正常返回
     * SC_NOT_MODIFIED : 未做任何修改
     */
    private final int[] OK_CODES = {HttpStatus.SC_OK, HttpStatus.SC_NOT_MODIFIED};

    /**
     * 响应码
     */
    private int code;

    /**
     * 响应结果
     */
    private String result;

    private Map<String, Object> responseHeaders;

    public HttpResult(int code, String result, Header[] headers) {
        this.code = code;
        this.result = result;
        if (headers != null) {
            responseHeaders = new HashMap<String, Object>(headers.length);
            for (Header header : headers) {
                responseHeaders.put(header.getName(), header.getValue());
            }
        }
    }

    /**
     * 错误响应结果
     *
     * @return HttpResult
     */
    public static HttpResult error() {
        return new HttpResult(ERROR_CODE, null, null);
    }

    /**
     * 响应是否出错
     *
     * @return 是否错误响应
     */
    public boolean isError() {
        for (int okCode : OK_CODES) {
            if (okCode == this.code) {
                return false;
            }
        }
        return true;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public Map<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, Object> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
