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
 * Dubbo Client 拦截器
 *
 * @author luanwenfei
 * @version 0.0.1 2021-02-05
 * @since 2021-02-05
 */
public class DubboConsumerClientInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.rpc.proxy.InvokerInvocationHandler";
    private static final String INTERCEPT_CLASS =
            "com.lubanops.apm.plugin.flowreplay.mockclient.define.dubbo.DubboConsumerClientInterceptor";

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
