/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.test.configelement.config;

import java.util.Map;

/**
 * 功能描述：请求参数类
 *
 * @author hjw
 * @since 2022-01-20
 */
public class HttpClientConfig {
    /**
     *  请求url
     */
    private final String url;
    /**
     *  请求参数
     */
    private final Map<String, String> headers;
    /**
     *  连接超时时间
     */
    private final int connectTimeout;
    /**
     *  读取超时
     */
    private final int socketTimeout;
    /**
     *  最大连接数
     */
    private final int maxTotal;
    /**
     *  路有数
     */
    private final int defaultMaxPerRoute;
    /**
     *  是否使用连接池
     */
    private final boolean isUseConnectPool;

    public HttpClientConfig(Builder builder) {
        this.url = builder.url;
        this.headers = builder.headers;
        this.connectTimeout = builder.connectTimeout;
        this.socketTimeout = builder.socketTimeout;
        this.maxTotal = builder.maxTotal;
        this.defaultMaxPerRoute = builder.defaultMaxPerRoute;
        this.isUseConnectPool = builder.isUseConnectPool;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public int getDefaultMaxPerRoute() {
        return defaultMaxPerRoute;
    }

    public boolean getUseConnectPool() {
        return isUseConnectPool;
    }

    public static class Builder {
        /**
         *  请求url
         */
        private String url;
        /**
         *  请求参数
         */
        private Map<String, String> headers;
        /**
         *  连接超时时间
         */
        private int connectTimeout;
        /**
         *  读取超时
         */
        private int socketTimeout;
        /**
         *  最大连接数
         */
        private int maxTotal;
        /**
         *  路有数
         */
        private int defaultMaxPerRoute;
        /**
         *  是否使用连接池
         */
        private boolean isUseConnectPool;

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        public Builder setDefaultMaxPerRoute(int defaultMaxPerRoute) {
            this.defaultMaxPerRoute = defaultMaxPerRoute;
            return this;
        }

        @SuppressWarnings("checkstyle:HiddenField")
        public Builder setUseConnectPool(boolean isUseConnectPool) {
            this.isUseConnectPool = isUseConnectPool;
            return this;
        }

        public HttpClientConfig build() {
            return new HttpClientConfig(this);
        }
    }
}
