/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.entity;

import com.huawei.flowcontrol.common.util.FilterUtil;

import java.util.Map;

/**
 * Http请求包装类
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class HttpRequestEntity implements RequestEntity {
    private String apiPath;

    private String pathInfo;

    private String servletPath;

    private Map<String, String> headers;

    private String method;

    public HttpRequestEntity(String pathInfo, String servletPath, Map<String, String> headers, String method) {
        this.pathInfo = pathInfo;
        this.servletPath = servletPath;
        this.headers = headers;
        this.method = method;
        this.apiPath = FilterUtil.filterTarget(pathInfo, servletPath);
    }

    public HttpRequestEntity(String apiPath, Map<String, String> headers, String method) {
        this.headers = headers;
        this.method = method;
        this.apiPath = apiPath;
    }

    public HttpRequestEntity() {
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public String getApiPath() {
        return apiPath;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
