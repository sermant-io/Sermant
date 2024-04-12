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

package com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.http;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Httpclient response result
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class HttpResult {
    /**
     * Error request response code
     */
    public static final int ERROR_CODE = -1;

    /**
     * Acceptable number SC_OK : Normal return, SC_NOT_MODIFIED : No changes were made
     */
    private static final int[] OK_CODES = {HttpStatus.SC_OK, HttpStatus.SC_NOT_MODIFIED};

    /**
     * Response code
     */
    private int code;

    /**
     * Response result
     */
    private String result;

    private Map<String, Object> responseHeaders;

    /**
     * Constructor
     *
     * @param code Response code
     * @param result Response result
     * @param headers Response result
     */
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
     * Error response result
     *
     * @return HttpResult
     */
    public static HttpResult error() {
        return new HttpResult(ERROR_CODE, null, null);
    }

    /**
     * Response error or not
     *
     * @return response result
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
