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

import com.huawei.discovery.entity.HttpAsyncContext;

/**
 * 异步httpclient工具类
 *
 * @author zhouss
 * @since 2022-10-11
 */
public class HttpAsyncUtils {
    private static final ThreadLocal<HttpAsyncContext> LOCAL = new ThreadLocal<>();

    private HttpAsyncUtils() {
    }

    /**
     * 创建上下文
     *
     * @return HttpAsyncContext
     */
    public static HttpAsyncContext getOrCreateContext() {
        final HttpAsyncContext asyncContext = LOCAL.get();
        if (asyncContext == null) {
            final HttpAsyncContext httpAsyncContext = new HttpAsyncContext();
            LOCAL.set(httpAsyncContext);
            return httpAsyncContext;
        }
        return asyncContext;
    }

    /**
     * 保存处理器
     *
     * @param handler 请求处理器
     */
    public static void saveHandler(Object handler) {
        final HttpAsyncContext httpAsyncContext = getOrCreateContext();
        httpAsyncContext.setHandler(handler);
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public static HttpAsyncContext getContext() {
        return LOCAL.get();
    }

    /**
     * 清理数据
     */
    public static void remove() {
        LOCAL.remove();
    }
}
