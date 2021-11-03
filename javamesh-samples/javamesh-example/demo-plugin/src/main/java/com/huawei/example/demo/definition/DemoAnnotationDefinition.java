/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.definition;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;

/**
 * 通过注解方式定位到拦截点的增强定义，本示例将测试构造函数、静态方法和示例方法三种拦截点
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoAnnotationDefinition implements EnhanceDefinition {
    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.annotationWith("com.huawei.example.demo.service.DemoAnnotation");
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
                )
        };
    }
}
