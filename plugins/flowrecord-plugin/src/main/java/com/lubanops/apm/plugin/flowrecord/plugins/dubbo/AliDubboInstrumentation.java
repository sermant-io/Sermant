/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.dubbo;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截点
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */
public class AliDubboInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "com.alibaba.dubbo.monitor.support.MonitorFilter";
    private static final String INTERCEPT_CLASS = "com.lubanops.apm.plugin.flowrecord.plugins.dubbo.DubboInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named("main"))
        };
    }
}
