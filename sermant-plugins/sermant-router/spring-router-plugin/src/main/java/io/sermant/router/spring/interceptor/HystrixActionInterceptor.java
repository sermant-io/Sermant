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

package io.sermant.router.spring.interceptor;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.router.common.request.RequestTag;
import io.sermant.router.common.utils.ThreadLocalUtils;

import java.util.Collections;

/**
 * HystrixContexSchedulerAction enhancement class, setting thread parameters
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class HystrixActionInterceptor extends AbstractInterceptor {
    private static final RequestTag EMPTY_REQUEST_HEADER = new RequestTag(Collections.emptyMap());

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments[0] instanceof HystrixConcurrencyStrategy) {
            if (!HystrixRequestContext.isCurrentThreadInitialized()) {
                HystrixRequestContext.initializeContext();
            }
            HystrixRequestVariableDefault<RequestTag> hystrixRequest = new HystrixRequestVariableDefault<>();
            RequestTag requestTag = ThreadLocalUtils.getRequestTag();

            // It is forbidden to deposit null, otherwise there will be serious performance problems
            hystrixRequest.set(requestTag == null ? EMPTY_REQUEST_HEADER : requestTag);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
