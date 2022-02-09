/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/StaticMethodsInter.java
 * and org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/StaticMethodsInterWithOverrideArgs.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.enhancer;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.common.OverrideArgumentsCall;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.Interceptor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 多Interceptor静态方法增强委派类
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
public final class StaticMethodEnhancer extends AbstractAroundEnhancer {

    @SuppressWarnings("checkstyle:ModifierOrder")
    private final static Logger LOGGER = LoggerFactory.getLogger();

    private final List<StaticMethodInterceptor> interceptors;

    public StaticMethodEnhancer(Interceptor originInterceptor, List<StaticMethodInterceptor> interceptors) {
        super(originInterceptor);
        this.interceptors = Collections.unmodifiableList(interceptors);
    }

    /**
     * 增强委派方法
     *
     * @param clazz     增强实例class对象
     * @param method    原方法
     * @param callable  原始调用
     * @param arguments 原方法参数
     * @return 增强后返回值
     * @throws Throwable 增强过程中产生的异常，当前实现使用原方法执行产生异常
     */
    @RuntimeType
    public Object intercept(@Origin Class<?> clazz,
            @Origin Method method,
            @Morph OverrideArgumentsCall callable,
            @AllArguments Object[] arguments) throws Throwable {
        return doIntercept(clazz, method, callable, arguments);
    }

    @Override
    protected BeforeResult doBefore(final EnhanceContext context) {
        BeforeResult beforeResult = new BeforeResult();
        for (StaticMethodInterceptor interceptor : interceptors) {
            context.increaseInvokedIndex();
            execBefore(interceptor, context, beforeResult);
            if (!beforeResult.isContinue()) {
                break;
            }
        }
        return beforeResult;
    }

    @Override
    protected void doOnThrow(final EnhanceContext context, final Throwable originThrowable) {
        for (int i = context.getInvokedIndex() - 1; i >= 0; i--) {
            StaticMethodInterceptor interceptor = interceptors.get(i);
            execOnThrow(interceptor, context, originThrowable);
        }
    }

    @Override
    protected Object doAfter(final EnhanceContext context, final Object result) {
        Object returnResult = result;
        for (int i = context.getInvokedIndex() - 1; i >= 0; i--) {
            StaticMethodInterceptor interceptor = interceptors.get(i);
            returnResult = execAfter(interceptor, context, returnResult);
        }
        return returnResult;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void execBefore(final StaticMethodInterceptor interceptor,
            final EnhanceContext context,
            final BeforeResult beforeResult) {
        Class<?> clazz = (Class<?>) context.getOrigin();
        Method method = context.getMethod();
        try {
            interceptor.before(clazz, method, context.getArguments(), beforeResult);
        } catch (Throwable t) {
            LOGGER.severe(String.format("An error occurred before [{%s}#{%s}] in interceptor [{%s}]: [{%s}]",
                    clazz.getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
            throwBizException(t);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void execOnThrow(final StaticMethodInterceptor interceptor,
            final EnhanceContext context,
            final Throwable originThrowable) {
        Class<?> clazz = (Class<?>) context.getOrigin();
        Method method = context.getMethod();
        try {
            interceptor.onThrow(clazz, method, context.getArguments(), originThrowable);
        } catch (Throwable t) {
            LOGGER.severe(String.format("An error occurred while handling throwable thrown by"
                            + " [{%s}#{%s}] in interceptor [{%s}]: [{%s}].",
                    clazz.getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
            throwBizException(t);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private Object execAfter(final StaticMethodInterceptor interceptor,
            final EnhanceContext context,
            final Object result) {
        Object returnResult = result;
        Class<?> clazz = (Class<?>) context.getOrigin();
        Method method = context.getMethod();
        try {
            returnResult = interceptor.after(clazz, method, context.getArguments(), returnResult);
        } catch (Throwable t) {
            LOGGER.severe(String.format("An error occurred after [{%s}#{%s}] in interceptor [{%s}]: [{%s}].",
                    clazz.getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
            throwBizException(t);
        }
        return returnResult;
    }
}
