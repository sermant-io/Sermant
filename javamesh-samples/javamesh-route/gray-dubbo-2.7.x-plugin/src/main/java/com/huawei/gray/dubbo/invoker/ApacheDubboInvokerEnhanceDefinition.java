/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.invoker;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 增强DubboInvoker类的doInvoke方法，更改路由信息
 *
 * @author l30008180
 * @since 2021年6月28日
 */
public class ApacheDubboInvokerEnhanceDefinition implements EnhanceDefinition {
    private static final String INVOKER_ENHANCE_CLASS = "org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker";

    private static final String INVOKER_INTERCEPT_CLASS
            = "com.huawei.gray.dubbo.invoker.ApacheDubboInvokerInstanceMethodInterceptor";

    private static final String ENHANCE_METHOD_NAME = "doInvoke";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(INVOKER_ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INVOKER_INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>named(ENHANCE_METHOD_NAME))};
    }
}
