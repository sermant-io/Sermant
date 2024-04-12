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
 * Asynchronous httpclient utility class
 *
 * @author zhouss
 * @since 2022-10-11
 */
public class HttpAsyncUtils {
    private static final ThreadLocal<HttpAsyncContext> LOCAL = new ThreadLocal<>();

    private HttpAsyncUtils() {
    }

    /**
     * Create a context
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
     * Save the processor
     *
     * @param handler Request Processor
     */
    public static void saveHandler(Object handler) {
        final HttpAsyncContext httpAsyncContext = getOrCreateContext();
        httpAsyncContext.setHandler(handler);
    }

    /**
     * Get context
     *
     * @return Context
     */
    public static HttpAsyncContext getContext() {
        return LOCAL.get();
    }

    /**
     * Clean your data
     */
    public static void remove() {
        LOCAL.remove();
    }
}
