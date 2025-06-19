/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.xds.traffic.management.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.utils.ClassUtils;

import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Add an injection interceptor by intercepting and inject a spring web interceptor
 *
 * @author lilai
 * @since 2025-06-14
 */
public class HandlerExecutionChainInterceptor extends AbstractInterceptor {
    private static final String AUTH_HANDLER_INTERCEPTOR
            = "io.sermant.xds.traffic.management.interceptor.AuthHandlerInterceptor";

    private volatile HandlerInterceptor handlerInterceptor;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object object = context.getObject();
        if (object instanceof HandlerExecutionChain) {
            HandlerExecutionChain chain = (HandlerExecutionChain) object;
            chain.addInterceptor(getInterceptor());
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    HandlerInterceptor getInterceptor() {
        if (handlerInterceptor == null) {
            synchronized (HandlerExecutionChainInterceptor.class) {
                if (handlerInterceptor == null) {
                    ClassUtils.defineClass(AUTH_HANDLER_INTERCEPTOR, getClass().getClassLoader());
                    handlerInterceptor = new AuthHandlerInterceptor();
                }
            }
        }
        return handlerInterceptor;
    }
}
