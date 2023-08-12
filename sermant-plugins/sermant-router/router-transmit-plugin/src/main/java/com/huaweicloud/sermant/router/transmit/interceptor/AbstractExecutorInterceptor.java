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
import com.huaweicloud.sermant.router.common.request.RequestHeader;
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

    private static final String CALLABLE_WRAPPER_CLASS_NAME = CallableWrapper.class.getName();

    private static final String RUNNABLE_AND_CALLABLE_WRAPPER_CLASS_NAME = RunnableAndCallableWrapper.class.getName();

    private static final String RUNNABLE_WRAPPER_CLASS_NAME = RunnableWrapper.class.getName();

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
        RequestHeader requestHeader = ThreadLocalUtils.getRequestHeader();
        RequestData requestData = ThreadLocalUtils.getRequestData();
        if (requestHeader == null && requestData == null) {
            return context;
        }
        Object argument = arguments[0];
        if (argument instanceof RunnableAndCallableWrapper || argument instanceof RunnableWrapper
                || argument instanceof CallableWrapper) {
            return context;
        }
        if (argument instanceof Runnable && argument instanceof Callable) {
            arguments[0] = getRunnableAndCallableWrapper(argument, requestHeader, requestData);
            return context;
        }
        if (argument instanceof Runnable) {
            arguments[0] = getRunnableWrapper(argument, requestHeader, requestData);
            return context;
        }
        if (argument instanceof Callable) {
            arguments[0] = getCallableWrapper(argument, requestHeader, requestData);
            return context;
        }
        return context;
    }

    private RunnableAndCallableWrapper<?> getRunnableAndCallableWrapper(Object argument, RequestHeader requestHeader,
            RequestData requestData) {
        log(argument, requestHeader, requestData, RUNNABLE_AND_CALLABLE_WRAPPER_CLASS_NAME);
        return new RunnableAndCallableWrapper<>((Runnable) argument, (Callable<?>) argument, requestHeader, requestData,
            cannotTransmit);
    }

    private RunnableWrapper<?> getRunnableWrapper(Object argument, RequestHeader requestHeader,
            RequestData requestData) {
        log(argument, requestHeader, requestData, RUNNABLE_WRAPPER_CLASS_NAME);
        return new RunnableWrapper<>((Runnable) argument, requestHeader, requestData, cannotTransmit);
    }

    private CallableWrapper<?> getCallableWrapper(Object argument, RequestHeader requestHeader,
            RequestData requestData) {
        log(argument, requestHeader, requestData, CALLABLE_WRAPPER_CLASS_NAME);
        return new CallableWrapper<>((Callable<?>) argument, requestHeader, requestData, cannotTransmit);
    }

    private void log(Object argument, RequestHeader requestHeader, RequestData requestData, String wrapperClassName) {
        LOGGER.log(Level.FINE, "Class name is {0}, hash code is {1}, requestTag is {2}, "
                + "requestData is {3}, will be converted to {4}.",
            new Object[]{argument.getClass().getName(), Integer.toHexString(argument.hashCode()),
                requestHeader, requestData, wrapperClassName});
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}