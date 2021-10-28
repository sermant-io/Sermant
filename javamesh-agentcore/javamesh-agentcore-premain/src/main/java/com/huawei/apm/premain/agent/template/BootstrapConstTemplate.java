/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.agent.template;

import java.lang.reflect.Constructor;
import java.util.List;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import com.huawei.apm.bootstrap.interceptors.ConstructorInterceptor;
import com.huawei.apm.bootstrap.lubanops.Interceptor;

/**
 * 启动类构造函数模板
 * <p>启动类加载器加载类的构造函数如果需要增强，则需要使用该模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/27
 */
public class BootstrapConstTemplate {
    /**
     * luban拦截器列表
     */
    private static List<? extends Interceptor> originInterceptors;

    /**
     * 拦截器列表
     */
    private static List<? extends ConstructorInterceptor> interceptors;

    /**
     * 初始化两组拦截器
     *
     * @param originInterceptors luban拦截器列表
     * @param interceptors       拦截器列表
     */
    public static void prepare(List<? extends Interceptor> originInterceptors,
            List<? extends ConstructorInterceptor> interceptors) {
        BootstrapConstTemplate.originInterceptors = originInterceptors;
        BootstrapConstTemplate.interceptors = interceptors;
    }

    /**
     * 方法执行前调用
     * <p>由于类加载器限制，需要使用反射调用外部方法，需要构建出动态advice类的全限定名，再用当前类加载器加载
     * <p>由于jvm重定义的限制，不能添加静态属性，动态advice类只能通过局部参数传递
     *
     * @param arguments   所有入参
     * @param constructor 构造函数本身
     * @param adviceCls   动态advice类
     * @throws Exception 发生异常
     */
    @Advice.OnMethodEnter
    public static void OnMethodEnter(
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.Origin Constructor<?> constructor,
            @Advice.Local(value = "ADVICE_CLS") Class<?> adviceCls) throws Exception {
        final StringBuilder sb = new StringBuilder()
                .append(constructor.getName())
                .append("(");
        for (Class<?> parameterType : constructor.getParameterTypes()) {
            sb.append(parameterType.getName()).append(',');
        }
        sb.append(')');
        final String adviceClsName = "com.huawei.apm.premain.agent.template.BootstrapConstTemplate_" +
                Integer.toHexString(sb.toString().hashCode());
        adviceCls = Thread.currentThread().getContextClassLoader().loadClass(adviceClsName);
        Object[] dynamicArgs = arguments;
        adviceCls.getDeclaredMethod("beforeConstructor", Object[].class, Constructor.class)
                .invoke(null, dynamicArgs, constructor);
        arguments = dynamicArgs;
    }

    /**
     * 调用luban拦截器的onStart方法
     *
     * @param arguments   所有入参
     * @param constructor 构造函数本身
     */
    public static void beforeConstructor(Object[] arguments, Constructor<?> constructor) {
        for (Interceptor interceptor : originInterceptors) {
            interceptor.onStart(null, arguments, constructor.getName(), "constructor");
        }
    }

    /**
     * 方法执行后调用
     *
     * @param obj       生成的对象
     * @param arguments 所有入参
     * @param adviceCls 动态advice类
     * @throws Exception 发生异常
     */
    @Advice.OnMethodExit
    public static void OnMethodExit(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.AllArguments Object[] arguments,
            @Advice.Local(value = "ADVICE_CLS") Class<?> adviceCls) throws Exception {
        adviceCls.getDeclaredMethod("afterConstructor", Object.class, Object[].class).invoke(null, obj, arguments);
    }

    /**
     * 调用luban拦截器的onFinally方法和构造拦截器的onConstruct方法
     *
     * @param obj       生成的对象
     * @param arguments 所有入参
     */
    public static void afterConstructor(Object obj, Object[] arguments) {
        for (Interceptor interceptor : originInterceptors) {
            interceptor.onFinally(obj, arguments, null, obj.getClass().getName(), "constructor");
        }
        for (ConstructorInterceptor interceptor : interceptors) {
            interceptor.onConstruct(obj, arguments);
        }
    }
}
