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

package io.sermant.flowcontrol.common.entity;

import io.sermant.flowcontrol.common.util.FilterUtil;

import java.util.Collections;
import java.util.Map;

/**
 * http request wrapper class
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class HttpRequestEntity extends AbstractRequestEntity {
    private String apiPath;

    private String pathInfo;

    private String servletPath;

    private Map<String, String> headers;

    private String method;

    private void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    private void setServletPath(String servletPath) {
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

    private void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String getMethod() {
        return method;
    }

    private void setMethod(String method) {
        this.method = method;
    }

    /**
     * builder
     *
     * @since 2022-07-20
     */
    public static class Builder {
        private final HttpRequestEntity httpRequestEntity;

        /**
         * 构建器
         */
        public Builder() {
            httpRequestEntity = new HttpRequestEntity();
        }

        /**
         * set service name
         *
         * @param serviceName service name
         * @return Builder
         */
        public Builder setServiceName(String serviceName) {
            this.httpRequestEntity.setServiceName(serviceName);
            return this;
        }

        /**
         * setting path information
         *
         * @param pathInfo path
         * @return Builder
         */
        public Builder setPathInfo(String pathInfo) {
            this.httpRequestEntity.setPathInfo(pathInfo);
            return this;
        }

        /**
         * set request path
         *
         * @param servletPath request path
         * @return Builder
         */
        public Builder setServletPath(String servletPath) {
            this.httpRequestEntity.setServletPath(servletPath);
            return this;
        }

        /**
         * set request header
         *
         * @param headers request header
         * @return Builder
         */
        public Builder setHeaders(Map<String, String> headers) {
            this.httpRequestEntity.setHeaders(Collections.unmodifiableMap(headers));
            return this;
        }

        /**
         * setting method type
         *
         * @param method method
         * @return Builder
         */
        public Builder setMethod(String method) {
            this.httpRequestEntity.setMethod(method);
            return this;
        }

        /**
         * set request direction
         *
         * @param requestType request direction
         * @return Builder
         */
        public Builder setRequestType(RequestType requestType) {
            this.httpRequestEntity.setRequestType(requestType);
            return this;
        }

        /**
         * set api
         *
         * @param apiPath api
         * @return Builder
         */
        public Builder setApiPath(String apiPath) {
            this.httpRequestEntity.apiPath = apiPath;
            return this;
        }

        /**
         * return the request for the build
         *
         * @return HttpRequestEntity
         * @throws IllegalArgumentException parameter exception throwing
         */
        public HttpRequestEntity build() {
            if (httpRequestEntity.apiPath == null) {
                if (httpRequestEntity.servletPath == null && httpRequestEntity.pathInfo == null) {
                    throw new IllegalArgumentException("Can not config request apiPath!");
                } else {
                    this.httpRequestEntity.apiPath = FilterUtil.filterTarget(httpRequestEntity.pathInfo,
                            httpRequestEntity.servletPath);
                }
            }
            return this.httpRequestEntity;
        }
    }
}
