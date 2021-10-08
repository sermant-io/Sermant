/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.boot;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截注解 org.springframework.boot.autoconfigure.SpringBootApplication的类,也可以拦截应用中启动后立即执行仅一次的类
 * 1、启动线程定时向kafka推送数据
 * 2、开启zookeeper监听器
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */
public class BootInstrumentation implements EnhanceDefinition {
    /**
     * 拦截点位于springbootapplication，仅触发一次
     */
    public static final String ENHANCE_ANNOTATION = "org.springframework.boot.autoconfigure.SpringBootApplication";
    private static final String INTERCEPT_CLASS = "com.lubanops.apm.plugin.flowrecord.plugins.boot.BootInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.annotationWith(ENHANCE_ANNOTATION);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named("main"))
        };
    }
}
