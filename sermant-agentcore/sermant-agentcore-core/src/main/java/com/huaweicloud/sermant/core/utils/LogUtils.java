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
 * 日志工具类
 *
 * @author lilai
 * @since 2023-03-24
 */
public class LogUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private LogUtils() {
    }

    /**
     * 打印Dubbo请求拦截Before切面日志
     *
     * @param context 拦截上下文
     */
    public static void printDubboRequestBeforePoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][Before] Dubbo request: %s",
                context));
    }

    /**
     * 打印Dubbo请求拦截After切面日志
     *
     * @param context 拦截上下文
     */
    public static void printDubboRequestAfterPoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][After] Dubbo request: %s",
                context));
    }

    /**
     * 打印Dubbo请求拦截OnThrow切面日志
     *
     * @param context 拦截上下文
     */
    public static void printDubboRequestOnThrowPoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][OnThrow] Dubbo request: %s",
                context));
    }

    /**
     * 打印HTTP请求拦截Before切面日志
     *
     * @param context 拦截上下文
     */
    public static void printHttpRequestBeforePoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][Before] HTTP request: %s",
                context));
    }

    /**
     * 打印HTTP请求拦截After切面日志
     *
     * @param context 拦截上下文
     */
    public static void printHttpRequestAfterPoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][After] HTTP request: %s",
                context));
    }

    /**
     * 打印HTTP请求拦截OnThrow切面日志
     *
     * @param context 拦截上下文
     */
    public static void printHttpRequestOnThrowPoint(ExecuteContext context) {
        LOGGER.finest(() -> String.format(Locale.ROOT, "[Request Intercepted by Sermant][OnThrow] HTTP request: %s",
                context));
    }
}
