/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.definition.apache;

import com.huawei.javamesh.core.agent.definition.EnhanceDefinition;
import com.huawei.javamesh.core.agent.definition.MethodInterceptPoint;
import com.huawei.javamesh.core.agent.matcher.ClassMatcher;
import com.huawei.javamesh.core.agent.matcher.ClassMatchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 增强ClusterUtils类的mergeUrl方法
 *
 * @author pengyuyi
 * @since 2021年6月28日
 */
public class ClusterDefinition implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.rpc.cluster.support.ClusterUtils";

    private static final String INTERCEPT_CLASS = "com.huawei.gray.dubbo.interceptor.apache.ClusterInterceptor";

    private static final String METHOD_NAME = "mergeUrl";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>named(METHOD_NAME))};
    }
}
