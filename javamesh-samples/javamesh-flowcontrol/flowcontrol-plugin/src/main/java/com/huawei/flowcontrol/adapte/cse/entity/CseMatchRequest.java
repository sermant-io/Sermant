/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.entity;

import java.util.Map;

/**
 * 匹配请求
 *
 * @author zhouss
 * @since 2021-11-24
 */
public class CseMatchRequest {
    private String apiPath;

    private Map<String, String> headers;

    private String httpMethod;

    public CseMatchRequest(String apiPath, Map<String, String> headers, String httpMethod) {
        this.apiPath = apiPath;
        this.headers = headers;
        this.httpMethod = httpMethod;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
}
