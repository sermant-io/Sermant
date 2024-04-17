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

package com.huawei.discovery.utils;

import java.net.URL;
import java.util.Map;

/**
 * Http Connection Thread context utility class
 *
 * @author zhouss
 * @since 2022-10-21
 */
public class HttpConnectionUtils {
    private static final ThreadLocal<HttpConnectionContext> LOCAL = new ThreadLocal<>();

    private HttpConnectionUtils() {
    }

    /**
     * Save contextual information
     *
     * @param context Context
     */
    public static void save(HttpConnectionContext context) {
        LOCAL.set(context);
    }

    /**
     * Get the context of a thread variable
     *
     * @return Context
     */
    public static HttpConnectionContext getContext() {
        return LOCAL.get();
    }

    /**
     * Clean your data
     */
    public static void remove() {
        LOCAL.remove();
    }

    /**
     * http url connection Context
     *
     * @since 2022-10-21
     */
    public static class HttpConnectionContext {
        /**
         * Resolved information, including the downstream service name and request path
         */
        private Map<String, String> urlInfo;

        /**
         * The most original URL
         */
        private URL originUrl;

        /**
         * Build context
         *
         * @param urlInfo Parse information
         * @param originUrl Original URL
         */
        public HttpConnectionContext(Map<String, String> urlInfo, URL originUrl) {
            this.urlInfo = urlInfo;
            this.originUrl = originUrl;
        }

        public Map<String, String> getUrlInfo() {
            return urlInfo;
        }

        public void setUrlInfo(Map<String, String> urlInfo) {
            this.urlInfo = urlInfo;
        }

        public URL getOriginUrl() {
            return originUrl;
        }

        public void setOriginUrl(URL originUrl) {
            this.originUrl = originUrl;
        }
    }
}
