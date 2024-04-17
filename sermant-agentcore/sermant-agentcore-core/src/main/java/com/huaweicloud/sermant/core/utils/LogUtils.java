/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * LogUtils
 *
 * @author lilai
 * @since 2023-03-24
 */
public class LogUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private LogUtils() {
    }

    /**
     * print Dubbo request on Before point
     *
     * @param context ExecuteContext
     */
    public static void printDubboRequestBeforePoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][Before] Dubbo request: %s",
                context));
    }

    /**
     * print Dubbo request on After point
     *
     * @param context ExecuteContext
     */
    public static void printDubboRequestAfterPoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][After] Dubbo request: %s",
                context));
    }

    /**
     * print Dubbo request on onThrow point
     *
     * @param context ExecuteContext
     */
    public static void printDubboRequestOnThrowPoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][OnThrow] Dubbo request: %s",
                context));
    }

    /**
     * print HTTP request on Before point
     *
     * @param context ExecuteContext
     */
    public static void printHttpRequestBeforePoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][Before] HTTP request: %s",
                context));
    }

    /**
     * print HTTP request on After point
     *
     * @param context ExecuteContext
     */
    public static void printHttpRequestAfterPoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][After] HTTP request: %s",
                context));
    }

    /**
     * print HTTP request on onThrow point
     *
     * @param context ExecuteContext
     */
    public static void printHttpRequestOnThrowPoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][OnThrow] HTTP request: %s",
                context));
    }
}
