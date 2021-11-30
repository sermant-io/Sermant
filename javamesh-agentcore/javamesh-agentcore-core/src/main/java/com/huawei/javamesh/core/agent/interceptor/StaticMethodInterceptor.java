package com.huawei.javamesh.core.agent.interceptor;

import com.huawei.javamesh.core.agent.common.BeforeResult;

import java.lang.reflect.Method;

/**
 * 静态方法拦截器接口
 */
public interface StaticMethodInterceptor extends Interceptor {

    /**
     * 前置方法
     *
     * @param clazz        增强实例class对象
     * @param method       原方法
     * @param arguments    原方法参数
     * @param beforeResult 前置结果
     * @throws Exception 前置方法异常
     */
    void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception;

    /**
     * 后置方法
     *
     * @param clazz     增强实例class对象
     * @param method    原方法
     * @param arguments 原方法参数
     * @param result    原方法结果
     * @throws Exception 后置方法异常
     */
    Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception;

    /**
     * 异常处理方法
     *
     * @param clazz     增强实例class对象
     * @param method    原方法
     * @param arguments 原方法参数
     * @param t         原方法异常
     */
    void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t);
}
