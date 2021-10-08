/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.dubbo;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * Alibaba dubbo provider 拦截器
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-28
 */
public class AlibabaDubboProviderClientInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "com.alibaba.dubbo.monitor.support.MonitorFilter";
    private static final String INTERCEPT_CLASS =
            "com.lubanops.apm.plugin.flowreplay.mockclient.define.dubbo.AlibabaDubboProviderClientInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named("invoke"))
        };
    }
}