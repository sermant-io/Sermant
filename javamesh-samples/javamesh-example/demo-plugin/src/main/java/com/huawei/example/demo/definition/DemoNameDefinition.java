/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.definition;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;

/**
 * 通过名称定位到拦截点的增强定义，本示例处理测试构造函数、静态方法和示例方法三种拦截点外，还会测试日志功能和统一配置
 * <p>本示例直接使用名称完全匹配的方式定位，其他名称相关的定位方式有：
 * <pre>
 *     1. {@link ClassMatchers#named(java.lang.String)}完全匹配
 *     2. {@link ClassMatchers#multiClass(java.lang.String...)}多重匹配
 *     3. {@link ClassMatchers#startWith(java.lang.String)}前缀匹配
 * </pre>
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoNameDefinition implements EnhanceDefinition {
    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named("com.huawei.example.demo.service.DemoNameService");
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newStaticMethodInterceptPoint(
                        "com.huawei.example.demo.interceptor.DemoStaticInterceptor",
                        ElementMatchers.<MethodDescription>named("staticFunc")
                ),
                MethodInterceptPoint.newConstructorInterceptPoint(
                        "com.huawei.example.demo.interceptor.DemoConstInterceptor",
                        ElementMatchers.<MethodDescription>any()
                ),
                MethodInterceptPoint.newInstMethodInterceptPoint(
                        "com.huawei.example.demo.interceptor.DemoInstInterceptor",
                        ElementMatchers.<MethodDescription>named("instFunc")
                ),
                MethodInterceptPoint.newStaticMethodInterceptPoint(
                        "com.huawei.example.demo.interceptor.DemoConfigInterceptor",
                        ElementMatchers.<MethodDescription>named("staticFunc")
                ),
                MethodInterceptPoint.newStaticMethodInterceptPoint(
                        "com.huawei.example.demo.interceptor.DemoLoggerInterceptor",
                        ElementMatchers.<MethodDescription>named("staticFunc")
                )
        };
    }
}
