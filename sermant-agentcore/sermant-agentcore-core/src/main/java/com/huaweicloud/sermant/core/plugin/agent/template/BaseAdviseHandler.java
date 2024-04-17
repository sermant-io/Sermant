/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent.template;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General basic Adviser
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class BaseAdviseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<String, List<Interceptor>> INTERCEPTOR_LIST_MAP = new ConcurrentHashMap<>();

    private BaseAdviseHandler() {
    }

    /**
     * Adviser logic of method enter
     *
     * @param context ExecuteContext
     * @param adviceKey adviceKey, consists of the class and method description, the advice template, and the
     * classLoader for the enhanced class
     *
     * @param enterHandler exception handler of onEnter
     * @return ExecuteContext
     * @throws Throwable Throwable
     */
    public static ExecuteContext handleMethodEnter(ExecuteContext context, String adviceKey,
            ExceptionHandler enterHandler) throws Throwable {
        List<Interceptor> interceptorList = INTERCEPTOR_LIST_MAP.get(adviceKey);
        if (interceptorList == null) {
            return context;
        }
        context.setInterceptorIterator(interceptorList.listIterator());
        return handleMethodEnter(context, context.getInterceptorIterator(), enterHandler);
    }

    /**
     * logic of onEnter
     *
     * @param context ExecuteContext
     * @param interceptorItr Interceptor bidirectional iterator
     * @param enterHandler exception handler of onEnter
     * @return ExecuteContext
     * @throws Throwable throws to the host instance
     */
    public static ExecuteContext handleMethodEnter(ExecuteContext context, ListIterator<Interceptor> interceptorItr,
            ExceptionHandler enterHandler) throws Throwable {
        ExecuteContext newContext = context;
        while (interceptorItr.hasNext()) {
            try {
                final Interceptor interceptor = interceptorItr.next();
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                            String.format(Locale.ROOT, "Method[%s] had been entered, interceptor is [%s].",
                                    MethodKeyCreator.getMethodKey(context.getMethod()),
                                    interceptor.getClass().getName()));
                }
                try {
                    final ExecuteContext tempContext = interceptor.before(newContext);
                    if (tempContext != null) {
                        newContext = tempContext;
                    }
                    if (newContext.isSkip()) {
                        return newContext;
                    }
                } catch (Throwable t) {
                    enterHandler.handle(context, interceptor, t);
                }
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Exception occurs when method enter.", exception);
                return newContext;
            }

            // Specifies that an exception is thrown to the host instance
            if (newContext.getThrowableOut() != null) {
                throw newContext.getThrowableOut();
            }
        }
        return newContext;
    }

    /**
     * Adviser logic of method exit
     *
     * @param context ExecuteContext
     * @param adviceKey adviceKey, consists of the class and method description, the advice template, and the
     * classLoader for the enhanced class
     *
     * @param throwHandler exception handler of onThrow
     * @param exitHandler exception handler of onExit
     * @return ExecuteContext
     * @throws Throwable Throwable
     */
    public static ExecuteContext handleMethodExit(ExecuteContext context, String adviceKey,
            ExceptionHandler throwHandler, ExceptionHandler exitHandler) throws Throwable {
        List<Interceptor> interceptorList = INTERCEPTOR_LIST_MAP.get(adviceKey);
        if (interceptorList == null) {
            return context;
        }
        return handleMethodExit(context, context.getInterceptorIterator(), throwHandler, exitHandler);
    }

    /**
     * logic for onExit&onThrow
     *
     * @param context ExecuteContext
     * @param interceptorItr Interceptor bidirectional iterator
     * @param throwHandler exception handler of onThrow
     * @param exitHandler exception handler of onExit
     * @return ExecuteContext
     * @throws Throwable throws to the host instance
     */
    public static ExecuteContext handleMethodExit(ExecuteContext context, ListIterator<Interceptor> interceptorItr,
            ExceptionHandler throwHandler, ExceptionHandler exitHandler) throws Throwable {
        ExecuteContext newContext = context;
        while (interceptorItr.hasPrevious()) {
            try {
                final Interceptor interceptor = interceptorItr.previous();
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                            String.format(Locale.ROOT, "Method[%s] had been exited, interceptor is [%s].",
                                    MethodKeyCreator.getMethodKey(context.getMethod()),
                                    interceptor.getClass().getName()));
                }
                if (newContext.getThrowable() != null && throwHandler != null) {
                    try {
                        final ExecuteContext tempContext = interceptor.onThrow(newContext);
                        if (tempContext != null) {
                            newContext = tempContext;
                        }
                    } catch (Throwable t) {
                        throwHandler.handle(newContext, interceptor, t);
                    }
                    if (newContext.getThrowableOut() != null) {
                        throw newContext.getThrowableOut();
                    }
                }
                try {
                    final ExecuteContext tempContext = interceptor.after(newContext);
                    if (tempContext != null) {
                        newContext = tempContext;
                    }
                } catch (Throwable t) {
                    exitHandler.handle(newContext, interceptor, t);
                }
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Exception occurs when method exit.", exception);
                return newContext;
            }
            if (newContext.getThrowableOut() != null) {
                throw newContext.getThrowableOut();
            }
        }
        return newContext;
    }

    public static Map<String, List<Interceptor>> getInterceptorListMap() {
        return INTERCEPTOR_LIST_MAP;
    }

    /**
     * Exception Handler Interface
     *
     * @since 2022-01-24
     */
    public interface ExceptionHandler {
        /**
         * Exception handling logic
         *
         * @param context ExecuteContext
         * @param interceptor Interceptor
         * @param throwable Throwable
         */
        void handle(ExecuteContext context, Interceptor interceptor, Throwable throwable);
    }
}
