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
import com.huawei.sermant.core.agent.interceptor.ConstructorInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.Interceptor;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * 启动类构造函数模板
 * <p>启动类加载器加载类的构造函数如果需要增强，则需要使用该模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/27
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@AboutDelete
@Deprecated
public class BootstrapConstTemplate {
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
    public static List<ConstructorInterceptor> INTERCEPTORS;

    /**
     * 方法执行前调用
     * <p>由于类加载器限制，需要使用反射调用外部方法，需要构建出动态advice类的全限定名，再用当前类加载器加载
     * <p>由于jvm重定义的限制，不能添加静态属性，动态advice类只能通过局部参数传递
     *
     * @param arguments           所有入参
     * @param constructor         构造函数本身
     * @param adviceCls           动态advice类
     * @param constInterceptorItr 构造拦截器双向迭代器
     * @throws Exception 发生异常
     */
    @SuppressWarnings({"checkstyle:MethodName", "checkstyle:OperatorWrap", "checkstyle:ParameterAssignment"})
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void OnMethodEnter(
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.Origin Constructor<?> constructor,
            @Advice.Local(value = "ADVICE_CLS") Class<?> adviceCls,
            @Advice.Local(value = "CONST_INTERCEPTOR_ITR") ListIterator<?> constInterceptorItr
    ) throws Exception {
        final StringBuilder builder = new StringBuilder()
                .append(constructor.getName())
                .append("(");
        final Type[] parameterTypes = constructor.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(parameterTypes[i].getTypeName());
        }
        builder.append(')');
        final String adviceClsName = "com.huawei.sermant.core.agent.template.BootstrapConstTemplate_" +
                Integer.toHexString(builder.toString().hashCode());
        adviceCls = ClassLoader.getSystemClassLoader().loadClass(adviceClsName);
        constInterceptorItr = (ListIterator<?>) adviceCls.getDeclaredMethod("getConstInterceptorItr").invoke(null);
        final Object[] dynamicArgs = arguments;
        adviceCls.getDeclaredMethod("beforeConstructor",
                Object[].class, Constructor.class, ListIterator.class
        ).invoke(null, dynamicArgs, constructor, constInterceptorItr);
        arguments = dynamicArgs;
    }

    /**
     * 方法执行后调用
     *
     * @param obj                 生成的对象
     * @param arguments           所有入参
     * @param adviceCls           动态advice类
     * @param constInterceptorItr 构造拦截器双向迭代器
     * @throws Exception 发生异常
     */
    @SuppressWarnings("checkstyle:MethodName")
    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void OnMethodExit(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.AllArguments Object[] arguments,
            @Advice.Local(value = "ADVICE_CLS") Class<?> adviceCls,
            @Advice.Local(value = "CONST_INTERCEPTOR_ITR") ListIterator<?> constInterceptorItr
    ) throws Exception {
        adviceCls.getDeclaredMethod("afterConstructor",
                Object.class, Object[].class, ListIterator.class
        ).invoke(null, obj, arguments, constInterceptorItr);
    }

    /**
     * 获取构造拦截器双向迭代器
     *
     * @return 构造拦截器双向迭代器
     */
    public static ListIterator<ConstructorInterceptor> getConstInterceptorItr() {
        return INTERCEPTORS.listIterator();
    }

    /**
     * 调用luban拦截器的onStart方法
     *
     * @param arguments           所有入参
     * @param constructor         构造函数本身
     * @param constInterceptorItr 构造拦截器双向迭代器
     */
    public static void beforeConstructor(Object[] arguments, Constructor<?> constructor,
            ListIterator<ConstructorInterceptor> constInterceptorItr) {
        final Object[] dynamicArgs = beforeOriginIntercept(arguments, constructor);
        if (dynamicArgs != arguments && dynamicArgs != null && dynamicArgs.length == arguments.length) {
            System.arraycopy(dynamicArgs, 0, arguments, 0, arguments.length);
        }
        beforeConstIntercept(arguments, constructor, constInterceptorItr);
    }

    /**
     * 调用luban拦截器的onStart方法
     *
     * @param arguments   所有入参
     * @param constructor 构造函数本身
     * @return 修正的参数列表
     */
    @SuppressWarnings({"checkstyle:MultipleStringLiterals", "checkstyle:IllegalCatch"})
    private static Object[] beforeOriginIntercept(Object[] arguments, Constructor<?> constructor) {
        if (ORIGIN_INTERCEPTOR == null) {
            return arguments;
        }
        try {
            final Object[] dynamicArgs = ORIGIN_INTERCEPTOR.onStart(
                    constructor.getDeclaringClass(), arguments, constructor.getName(), "constructor");
            if (dynamicArgs != null && dynamicArgs.length == arguments.length) {
                return dynamicArgs;
            }
        } catch (Throwable t) {
            LOGGER.severe(String.format(Locale.ROOT,
                    "invoke onStart method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    constructor.getDeclaringClass().getName(), "constructor", t.getMessage()));
        }
        return arguments;
    }

    /**
     * 构造拦截器空迭代（预留拓展空间）
     *
     * @param arguments           所有入参
     * @param constructor         构造函数本身
     * @param constInterceptorItr 构造拦截器双向迭代器
     */
    @SuppressWarnings("checkstyle:RegexpMultiline")
    private static void beforeConstIntercept(Object[] arguments, Constructor<?> constructor,
            ListIterator<ConstructorInterceptor> constInterceptorItr) {
        while (constInterceptorItr.hasNext()) {
            constInterceptorItr.next();
            // do something maybe
        }
    }

    /**
     * 调用luban拦截器的onFinally方法和构造拦截器的onConstruct方法
     *
     * @param obj                 生成的对象
     * @param arguments           所有入参
     * @param constInterceptorItr 构造拦截器双向迭代器
     */
    public static void afterConstructor(Object obj, Object[] arguments,
            ListIterator<ConstructorInterceptor> constInterceptorItr) {
        afterConstIntercept(obj, arguments, constInterceptorItr);
        afterOriginIntercept(obj, arguments);
    }

    /**
     * 调用luban拦截器的onFinally方法
     *
     * @param obj       生成的对象
     * @param arguments 所有入参
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private static void afterOriginIntercept(Object obj, Object[] arguments) {
        if (ORIGIN_INTERCEPTOR == null) {
            return;
        }
        try {
            ORIGIN_INTERCEPTOR.onFinally(obj, arguments, null, obj.getClass().getName(), "constructor");
        } catch (Throwable t) {
            LOGGER.severe(String.format(Locale.ROOT,
                    "invoke onFinally method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    obj.getClass().getName(), "constructor", t.getMessage()));
        }
    }

    /**
     * 调用构造拦截器的onConstruct方法
     *
     * @param obj                 生成的对象
     * @param arguments           所有入参
     * @param constInterceptorItr 构造拦截器双向迭代器
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private static void afterConstIntercept(Object obj, Object[] arguments,
            ListIterator<ConstructorInterceptor> constInterceptorItr) {
        while (constInterceptorItr.hasPrevious()) {
            final ConstructorInterceptor interceptor = constInterceptorItr.previous();
            try {
                interceptor.onConstruct(obj, arguments);
            } catch (Throwable t) {
                LOGGER.severe(String.format(Locale.ROOT,
                        "An error occurred on construct [{%s}] in interceptor [{%s}]: [{%s}]",
                        obj.getClass().getName(), interceptor.getClass().getName(), t.getMessage()));
            }
        }
    }
}
