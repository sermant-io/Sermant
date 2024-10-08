/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.router.common.utils.ThreadLocalUtils;
import reactor.netty.ConnectionObserver.State;

/**
 * When intercepting the HttpServerHandle and only introducing spring-boot-starter-webflux for reactive programming,
 * you need to remove the thread variable in the post-method
 * <p>spring cloud Greenwich.RELEASE+
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class HttpServerHandleInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (shouldHandle(context)) {
            ThreadLocalUtils.removeRequestData();
            ThreadLocalUtils.removeRequestTag();
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (shouldHandle(context)) {
            ThreadLocalUtils.removeRequestData();
            ThreadLocalUtils.removeRequestTag();
        }
        return context;
    }

    private boolean shouldHandle(ExecuteContext context) {
        Object argument = context.getArguments()[1];
        if (argument instanceof State) {
            State state = (State) argument;
            return state == State.DISCONNECTING;
        }
        return false;
    }
}
