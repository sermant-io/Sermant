/*
 *   Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.crossthread.interceptors;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.utils.tag.TrafficData;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.crossthread.pojo.TrafficMessage;
import com.huaweicloud.sermant.tag.transmission.crossthread.wrapper.CallableWrapper;
import com.huaweicloud.sermant.tag.transmission.crossthread.wrapper.RunnableAndCallableWrapper;
import com.huaweicloud.sermant.tag.transmission.crossthread.wrapper.RunnableWrapper;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * thread pool interceptor abstract class
 *
 * @author provenceee
 * @since 2023-06-08
 */
public abstract class AbstractExecutorInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String RUNNABLE_WRAPPER_CLASS_NAME = RunnableWrapper.class.getCanonicalName();

    private static final String CALLABLE_WRAPPER_CLASS_NAME = CallableWrapper.class.getCanonicalName();

    private static final String RUNNABLE_AND_CALLABLE_WRAPPER_CLASS_NAME =
            RunnableAndCallableWrapper.class.getCanonicalName();

    private final boolean cannotTransmit;

    /**
     * construction method
     *
     * @param cannotTransmit Whether thread variables need to be deleted before executing the method
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
        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        TrafficData trafficData = TrafficUtils.getTrafficData();
        if (trafficTag == null && trafficData == null) {
            return context;
        }
        TrafficMessage trafficMessage = new TrafficMessage(trafficTag, trafficData);
        Object executorObject = context.getObject();
        String executorName = executorObject.getClass().getSimpleName();
        Object argument = arguments[0];
        if (argument instanceof RunnableAndCallableWrapper || argument instanceof RunnableWrapper
                || argument instanceof CallableWrapper) {
            return context;
        }
        if (argument instanceof Runnable && argument instanceof Callable) {
            return buildRunnableAndCallableWrapper(context, arguments, trafficMessage, argument, executorName);
        }
        if (argument instanceof Runnable) {
            return buildRunnableWrapper(context, arguments, trafficMessage, argument, executorName);
        }
        if (argument instanceof Callable) {
            return buildCallableWrapper(context, arguments, trafficMessage, argument, executorName);
        }
        return context;
    }

    private ExecuteContext buildCallableWrapper(ExecuteContext context, Object[] arguments,
            TrafficMessage trafficMessage,
            Object argument,
            String executorName) {
        log(argument, trafficMessage, CALLABLE_WRAPPER_CLASS_NAME);
        arguments[0] = new CallableWrapper<>((Callable<?>) argument, trafficMessage,
                cannotTransmit, executorName);
        return context;
    }

    private ExecuteContext buildRunnableWrapper(ExecuteContext context, Object[] arguments,
            TrafficMessage trafficMessage,
            Object argument,
            String executorName) {
        log(argument, trafficMessage, RUNNABLE_WRAPPER_CLASS_NAME);
        arguments[0] = new RunnableWrapper<>((Runnable) argument, trafficMessage,
                cannotTransmit, executorName);
        return context;
    }

    private ExecuteContext buildRunnableAndCallableWrapper(ExecuteContext context, Object[] arguments,
            TrafficMessage trafficMessage, Object argument, String executorName) {
        log(argument, trafficMessage, RUNNABLE_AND_CALLABLE_WRAPPER_CLASS_NAME);
        arguments[0] = new RunnableAndCallableWrapper<>((Runnable) argument, (Callable<?>) argument,
                trafficMessage, cannotTransmit, executorName);
        return context;
    }

    private void log(Object argument, TrafficMessage trafficMessage, String wrapperClassName) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Class name is {0}, hash code is {1}, trafficTag is {2}, "
                            + "trafficData is {3}, will be converted to {4}.",
                    new Object[]{argument.getClass().getName(), Integer.toHexString(argument.hashCode()),
                            trafficMessage.getTrafficTag(), trafficMessage.getTrafficData(), wrapperClassName});
        }
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}