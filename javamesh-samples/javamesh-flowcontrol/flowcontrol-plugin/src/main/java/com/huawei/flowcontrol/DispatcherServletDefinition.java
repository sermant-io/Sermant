/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截org.springframework.web.servlet.DispatcherServlet类
 *
 * @author liyi
 * @since 2020-08-26
 */
public class DispatcherServletDefinition implements EnhanceDefinition {
    /**
     * 增强类的全限定名
     */
    private static final String ENHANCE_CLASS = "org.springframework.web.servlet.DispatcherServlet";

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = "com.lubanops.apm.plugin.flowcontrol.DispatcherServletInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
            MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named("doService"))
        };
    }

}
