/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.agent.template;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.Interceptor;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * 启动类静态方法模板
 * <p>启动类加载器加载类的静态方法如果需要增强，则需要使用该模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/27
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@AboutDelete
@Deprecated
public class BootstrapStaticTemplate {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * luban拦截器
     */
    @SuppressWarnings({"checkstyle:DeclarationOrder", "checkstyle:VisibilityModifier", "checkstyle:StaticVariableName"})
    public static Interceptor ORIGIN_INTERCEPTOR;

    /**
     * 拦截器列表
     */
    @SuppressWarnings({"checkstyle:DeclarationOrder", "checkstyle:VisibilityModifier", "checkstyle:StaticVariableName"})
    public static List<StaticMethodInterceptor> INTERCEPTORS;

    /**
     * 方法执行前调用
     * <p>由于类加载器限制，需要使用反射调用外部方法，需要构建出动态advice类的全限定名，再用当前类加载器加载
     * <p>由于jvm重定义的限制，不能添加静态属性，动态advice类只能通过局部参数传递
     *
     * @param cls                  被增强的类
     * @param method               被增强方法
     * @param arguments            所有参数
     * @param adviceCls            动态advice类
     * @param staticInterceptorItr 静态插件的双向迭代器
     * @return 是否进行主要流程
     * @throws Exception 发生异常
     */
    @SuppressWarnings({"checkstyle:MethodName", "checkstyle:ParameterAssignment", "checkstyle:OperatorWrap"})
    @Advice.OnMethodEnter(suppress = Throwable.class, skipOn = Advice.OnDefaultValue.class)
    public static boolean OnMethodEnter(
            @Advice.Origin Class<?> cls,
            @Advice.Origin Method method,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.Local(value = "ADVICE_CLS") Class<?> adviceCls,
            @Advice.Local(value = "STATIC_INTERCEPTOR_ITR") ListIterator<?> staticInterceptorItr
    ) throws Exception {
        final StringBuilder builder = new StringBuilder()
                .append(method.getDeclaringClass().getName())
                .append('#')
                .append(method.getName())
                .append("(");
        final Type[] parameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(parameterTypes[i].getTypeName());
        }
        builder.append(')');
        final String adviceClsName = "com.huawei.sermant.core.agent.template.BootstrapStaticTemplate_" +
                Integer.toHexString(builder.toString().hashCode());
        adviceCls = ClassLoader.getSystemClassLoader().loadClass(adviceClsName);
        staticInterceptorItr = (ListIterator<?>) adviceCls.getDeclaredMethod("getStaticInterceptorItr").invoke(null);
        final Object[] dynamicArgs = arguments;
        final Boolean res = (Boolean) adviceCls.getDeclaredMethod("beforeStaticMethod",
                Class.class, Method.class, Object[].class, ListIterator.class
        ).invoke(null, cls, method, dynamicArgs, staticInterceptorItr);
        arguments = dynamicArgs;
        return res;
    }

    /**
     * 方法执行后调用
     *
     * @param cls                  被拦截的类
     * @param method               被拦截方法
     * @param arguments            所有参数
     * @param result               调用结果
     * @param throwable            抛出异常
     * @param adviceCls            动态advice类
     * @param staticInterceptorItr 静态插件的双向迭代器
     * @throws Exception 调用异常
     */
    @SuppressWarnings({"checkstyle:MethodName", "checkstyle:ParameterAssignment", "checkstyle:ParameterNumber"})
    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
    public static void OnMethodExit(
            @Advice.Origin Class<?> cls,
            @Advice.Origin Method method,
            @Advice.AllArguments Object[] arguments,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
            @Advice.Thrown Throwable throwable,
            @Advice.Local(value = "ADVICE_CLS") Class<?> adviceCls,
            @Advice.Local(value = "STATIC_INTERCEPTOR_ITR") ListIterator<?> staticInterceptorItr
    ) throws Exception {
        result = adviceCls.getDeclaredMethod("afterStaticMethod",
                Class.class, Method.class, Object[].class, Object.class, Throwable.class, ListIterator.class
        ).invoke(null, cls, method, arguments, result, throwable, staticInterceptorItr);
    }

    /**
     * 获取静态插件的双向迭代器
     *
     * @return 静态插件的双向迭代器
     */
    public static ListIterator<StaticMethodInterceptor> getStaticInterceptorItr() {
        return INTERCEPTORS.listIterator();
    }

    /**
     * 调用luban拦截器的onStart方法和静态拦截器的before方法
     *
     * @param cls                  被拦截的类
     * @param method               被拦截的方法
     * @param arguments            所有参数
     * @param staticInterceptorItr 静态插件的双向迭代器
     * @return 是否进行主要流程
     */
    public static boolean beforeStaticMethod(Class<?> cls, Method method, Object[] arguments,
            ListIterator<StaticMethodInterceptor> staticInterceptorItr) {
        final Object[] dynamicArgs = beforeOriginIntercept(cls, method, arguments);
        if (dynamicArgs != arguments && dynamicArgs != null && dynamicArgs.length == arguments.length) {
            System.arraycopy(dynamicArgs, 0, arguments, 0, arguments.length);
        }
        return beforeStaticIntercept(cls, method, arguments, staticInterceptorItr);
    }

    /**
     * 调用luban拦截器的onStart方法
     *
     * @param cls       被拦截的类
     * @param method    被拦截的方法
     * @param arguments 所有参数
     * @return 修正的参数列表
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private static Object[] beforeOriginIntercept(Class<?> cls, Method method, Object[] arguments) {
        if (ORIGIN_INTERCEPTOR == null) {
            return arguments;
        }
        try {
            final Object[] dynamicArgs = ORIGIN_INTERCEPTOR.onStart(cls, arguments, cls.getName(), method.getName());
            if (dynamicArgs != null && dynamicArgs.length == arguments.length) {
                return dynamicArgs;
            }
        } catch (Throwable t) {
            LOGGER.severe(String.format(Locale.ROOT,
                    "invoke onStart method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    cls.getName(), method.getName(), t.getMessage()));
        }
        return arguments;
    }

    /**
     * 调用静态拦截器的before方法
     *
     * @param cls                  被拦截的类
     * @param method               被拦截的方法
     * @param arguments            所有参数
     * @param staticInterceptorItr 静态插件的双向迭代器
     * @return 是否进行主要流程
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private static boolean beforeStaticIntercept(Class<?> cls, Method method, Object[] arguments,
            ListIterator<StaticMethodInterceptor> staticInterceptorItr) {
        final BeforeResult beforeResult = new BeforeResult();
        while (staticInterceptorItr.hasNext()) {
            final StaticMethodInterceptor interceptor = staticInterceptorItr.next();
            try {
                interceptor.before(cls, method, arguments, beforeResult);
            } catch (Throwable t) {
                LOGGER.severe(String.format(Locale.ROOT,
                        "An error occurred before [{%s}#{%s}] in interceptor [{%s}]: [{%s}]",
                        cls.getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
            }
            if (!beforeResult.isContinue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 调用luban拦截器的onFinally、onError方法和静态拦截器的after、onThrow方法
     *
     * @param cls                  被拦截的类
     * @param method               被拦截方法
     * @param arguments            所有参数
     * @param result               调用结果
     * @param throwable            抛出异常
     * @param staticInterceptorItr 静态插件的双向迭代器
     * @return 调用结果
     */
    @SuppressWarnings({"checkstyle:ParameterNumber", "checkstyle:ParameterAssignment"})
    public static Object afterStaticMethod(Class<?> cls, Method method, Object[] arguments, Object result,
            Throwable throwable, ListIterator<StaticMethodInterceptor> staticInterceptorItr) {
        result = afterStaticIntercept(cls, method, arguments, result, throwable, staticInterceptorItr);
        afterOriginIntercept(cls, method, arguments, result, throwable);
        return result;
    }

    /**
     * 调用luban拦截器的onFinally、onError方法
     *
     * @param cls       被拦截的类
     * @param method    被拦截方法
     * @param arguments 所有参数
     * @param result    调用结果
     * @param throwable 抛出异常
     */
    @SuppressWarnings({"checkstyle:RegexpMultiline", "checkstyle:IllegalCatch"})
    private static void afterOriginIntercept(Class<?> cls, Method method, Object[] arguments, Object result,
            Throwable throwable) {
        if (ORIGIN_INTERCEPTOR == null) {
            return;
        }
        if (throwable != null) {
            try {
                ORIGIN_INTERCEPTOR.onError(null, arguments, throwable, cls.getName(), method.getName());
            } catch (Throwable t) {
                LOGGER.severe(String.format(Locale.ROOT,
                        "invoke onError method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                        cls.getName(), method.getName(), t.getMessage()));
            }
        }
        try {
            ORIGIN_INTERCEPTOR.onFinally(null, arguments, result, cls.getName(), method.getName());
        } catch (Throwable t) {
            LOGGER.severe(String.format(Locale.ROOT,
                    "invoke onFinally method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    cls.getName(), method.getName(), t.getMessage()));
        }
    }

    /**
     * 调用静态拦截器的after、onThrow方法
     *
     * @param cls                  被拦截的类
     * @param method               被拦截方法
     * @param arguments            所有参数
     * @param result               调用结果
     * @param throwable            抛出异常
     * @param staticInterceptorItr 静态插件的双向迭代器
     * @return 调用结果
     */
    @SuppressWarnings({"checkstyle:OperatorWrap", "ParameterNumber", "IllegalCatch", "ParameterAssignment"})
    private static Object afterStaticIntercept(Class<?> cls, Method method, Object[] arguments, Object result,
            Throwable throwable, ListIterator<StaticMethodInterceptor> staticInterceptorItr) {
        while (staticInterceptorItr.hasPrevious()) {
            final StaticMethodInterceptor interceptor = staticInterceptorItr.previous();
            if (throwable != null) {
                try {
                    interceptor.onThrow(cls, method, arguments, throwable);
                } catch (Throwable t) {
                    LOGGER.severe(String.format(Locale.ROOT, "An error occurred " +
                                    "while handling throwable thrown by [{%s}#{%s}] in interceptor [{%s}]: [{%s}].",
                            cls.getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
                }
            }
            try {
                result = interceptor.after(cls, method, arguments, result);
            } catch (Throwable t) {
                LOGGER.severe(String.format(Locale.ROOT,
                        "An error occurred after [{%s}#{%s}] in interceptor [{%s}]: [{%s}].",
                        cls.getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
            }
        }
        return result;
    }
}
