/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.agent.template;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.logging.Logger;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.apm.core.common.LoggerFactory;
import com.huawei.apm.core.lubanops.bootstrap.Interceptor;

/**
 * 启动类静态方法模板
 * <p>启动类加载器加载类的静态方法如果需要增强，则需要使用该模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/27
 */
public class BootstrapStaticTemplate {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * luban拦截器
     */
    public static Interceptor ORIGIN_INTERCEPTOR;

    /**
     * 拦截器列表
     */
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
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(parameterTypes[i].getName());
        }
        builder.append(')');
        final String adviceClsName = "com.huawei.apm.core.agent.template.BootstrapStaticTemplate_" +
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
