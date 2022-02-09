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

package com.huawei.sermant.core.plugin.agent.template;

import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;

import java.util.ListIterator;

/**
 * 通用的基础Adviser
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class CommonBaseAdviser {
    private CommonBaseAdviser() {
    }

    /**
     * 前置触发点
     *
     * @param context        执行上下文
     * @param interceptorItr 拦截器双向迭代器
     * @param beforeHandler  before的异常处理器
     * @return 执行上下文
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    public static ExecuteContext onMethodEnter(ExecuteContext context, ListIterator<Interceptor> interceptorItr,
            ExceptionHandler beforeHandler) {
        ExecuteContext newContext = context;
        while (interceptorItr.hasNext()) {
            final Interceptor interceptor = interceptorItr.next();
            try {
                final ExecuteContext tempContext = interceptor.before(newContext);
                if (tempContext != null) {
                    newContext = tempContext;
                }
                if (newContext.isSkip()) {
                    return newContext;
                }
            } catch (Throwable t) {
                beforeHandler.handle(context, interceptor, t);
            }
        }
        return newContext;
    }

    /**
     * 后置触发点
     *
     * @param context        执行上下文
     * @param interceptorItr 拦截器双向迭代器
     * @param onThrowHandler onThrow的异常处理器
     * @param afterHandler   after的的异常处理器
     * @return 执行上下文
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    public static ExecuteContext onMethodExit(ExecuteContext context, ListIterator<Interceptor> interceptorItr,
            ExceptionHandler onThrowHandler, ExceptionHandler afterHandler) {
        ExecuteContext newContext = context;
        while (interceptorItr.hasPrevious()) {
            final Interceptor interceptor = interceptorItr.previous();
            if (newContext.getThrowable() != null && onThrowHandler != null) {
                try {
                    final ExecuteContext tempContext = interceptor.onThrow(newContext);
                    if (tempContext != null) {
                        newContext = tempContext;
                    }
                } catch (Throwable t) {
                    onThrowHandler.handle(newContext, interceptor, t);
                }
            }
            try {
                final ExecuteContext tempContext = interceptor.after(newContext);
                if (tempContext != null) {
                    newContext = tempContext;
                }
            } catch (Throwable t) {
                afterHandler.handle(newContext, interceptor, t);
            }
        }
        return newContext;
    }

    /**
     * 异常处理器
     */
    public interface ExceptionHandler {
        void handle(ExecuteContext context, Interceptor interceptor, Throwable throwable);
    }
}
