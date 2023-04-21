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

package com.huaweicloud.sermant.router.transmit.interceptor;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.transmit.wrapper.CallableWrapper;
import com.huaweicloud.sermant.router.transmit.wrapper.RunnableAndCallableWrapper;
import com.huaweicloud.sermant.router.transmit.wrapper.RunnableWrapper;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 线程池拦截器抽象类
 *
 * @author provenceee
 * @since 2023-06-08
 */
public abstract class AbstractExecutorInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final boolean cannotTransmit;

    /**
     * 构造方法
     *
     * @param cannotTransmit 执行方法之前是否需要删除线程变量
     */
    protected AbstractExecutorInterceptor(boolean cannotTransmit) {
        this.cannotTransmit = cannotTransmit;
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length == 0 || arguments[0] == null) {
            return context;
        }
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        RequestData requestData = ThreadLocalUtils.getRequestData();
        if (requestTag != null || requestData != null) {
            Object argument = arguments[0];
            if (argument instanceof RunnableAndCallableWrapper || argument instanceof RunnableWrapper
                    || argument instanceof CallableWrapper) {
                return context;
            }
            if (argument instanceof Runnable && argument instanceof Callable) {
                log(argument, requestTag, requestData, "runnableAndCallableWrapper");
                arguments[0] = new RunnableAndCallableWrapper<>((Runnable) argument, (Callable<?>) argument,
                        requestTag, requestData, cannotTransmit);
                return context;
            }
            if (argument instanceof Runnable) {
                log(argument, requestTag, requestData, "runnableWrapper");
                arguments[0] = new RunnableWrapper<>((Runnable) argument, requestTag, requestData,
                        cannotTransmit);
                return context;
            }
            if (argument instanceof Callable) {
                log(argument, requestTag, requestData, "callableWrapper");
                arguments[0] = new CallableWrapper<>((Callable<?>) argument, requestTag, requestData,
                        cannotTransmit);
                return context;
            }
        }
        return context;
    }

    private void log(Object argument, RequestTag requestTag, RequestData requestData, String wrapperClassName) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Class name is {0}, hash code is {1}, requestTag is {2}, "
                            + "requestData is {3}, will be converted to {4}.",
                    new Object[]{argument.getClass().getName(), Integer.toHexString(argument.hashCode()),
                            requestTag, requestData, wrapperClassName});
        }
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}