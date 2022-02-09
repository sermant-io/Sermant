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
 * Based on org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/InstMethodsInter.java,
 * org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/InstMethodsInterWithOverrideArgs.java,
 * org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/StaticMethodsInter.java
 * and org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/StaticMethodsInterWithOverrideArgs.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.enhancer;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.Interceptor;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * 原生插件适配委托类
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
public abstract class OriginEnhancer {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    protected final Interceptor interceptor;

    protected OriginEnhancer(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    protected Object[] onStart(Object obj, Object[] allArguments, Method method) {
        if (interceptor == null) {
            return allArguments;
        }
        String className = resolveClassName(obj);
        try {
            Object[] newArguments = interceptor.onStart(obj, allArguments, className, method.getName());
            if (newArguments != null && newArguments.length == allArguments.length) {
                return newArguments;
            }
        } catch (Throwable t) {
            LOGGER.severe(
                    String.format("invoke onStart method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                            className, method.getName(), t.getMessage()));
        }
        return allArguments;
    }

    @SuppressWarnings({"checkstyle:IllegalCatch", "checkstyle:RegexpMultiline"})
    protected void onFinally(Object obj, Object[] allArguments, Method method, Object result) {
        if (interceptor == null) {
            return;
        }
        String className = resolveClassName(obj);
        final String methodName = method == null ? "constructor" : method.getName();
        try {
            interceptor.onFinally(obj, allArguments, result, className, methodName);
            // 返回结果不生效！
        } catch (Exception t) {
            LOGGER.severe(String.format(
                    "invoke onFinally method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    className, methodName, t.getMessage()));
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void onError(Object obj, Object[] allArguments, Method method, Throwable throwable) {
        if (interceptor == null) {
            return;
        }
        String className = resolveClassName(obj);
        try {
            interceptor.onError(obj, allArguments, throwable, className, method.getName());
        } catch (Throwable t) {
            LOGGER.severe(String.format(
                    "invoke onError method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    className, method.getName(), t.getMessage()));
        }
    }

    private String resolveClassName(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof Class) {
            return ((Class<?>) obj).getName();
        }
        return obj.getClass().getName();
    }
}
