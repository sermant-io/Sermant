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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import java.util.Collections;

/**
 * HystrixContexSchedulerAction增强类，设置线程参数
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class HystrixActionInterceptor extends AbstractInterceptor {
    private static final RequestHeader EMPTY_REQUEST_HEADER = new RequestHeader(Collections.emptyMap());

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments[0] instanceof HystrixConcurrencyStrategy) {
            if (!HystrixRequestContext.isCurrentThreadInitialized()) {
                HystrixRequestContext.initializeContext();
            }
            HystrixRequestVariableDefault<RequestHeader> hystrixRequest = new HystrixRequestVariableDefault<>();
            RequestHeader requestHeader = ThreadLocalUtils.getRequestHeader();

            // 禁止存入null，否则会有严重的性能问题
            hystrixRequest.set(requestHeader == null ? EMPTY_REQUEST_HEADER : requestHeader);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}