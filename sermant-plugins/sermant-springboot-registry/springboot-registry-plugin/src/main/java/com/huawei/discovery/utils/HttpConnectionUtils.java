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
 * Http Connection 线程上下文工具类
 *
 * @author zhouss
 * @since 2022-10-21
 */
public class HttpConnectionUtils {
    private static final ThreadLocal<HttpConnectionContext> LOCAL = new ThreadLocal<>();

    private HttpConnectionUtils() {
    }

    /**
     * 保存上下文信息
     *
     * @param context 上下文
     */
    public static void save(HttpConnectionContext context) {
        LOCAL.set(context);
    }

    /**
     * 获取线程变量的上下文
     *
     * @return 上下文
     */
    public static HttpConnectionContext getContext() {
        return LOCAL.get();
    }

    /**
     * 清理数据
     */
    public static void remove() {
        LOCAL.remove();
    }

    /**
     * http url connection上下文
     *
     * @since 2022-10-21
     */
    public static class HttpConnectionContext {
        /**
         * 已解析的信息, 包含下游服务名和请求路径
         */
        private Map<String, String> urlInfo;

        /**
         * 最原始的url
         */
        private URL originUrl;

        /**
         * 构建上下文
         *
         * @param urlInfo 解析信息
         * @param originUrl 原始url
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
