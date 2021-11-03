/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.registry;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 增强RegistryDirectory类的notify方法，获取应用缓存的路由信息
 *
 * @author l30008180
 * @since 2021年6月28日
 */
public class ApacheDubboRegistryEnhanceDefinition implements EnhanceDefinition {
    private static final String REGISTRY_ENHANCE_CLASS = "org.apache.dubbo.registry.integration.RegistryDirectory";

    private static final String REGISTRY_INTERCEPT_CLASS
            = "com.huawei.apm.plugin.gray.dubbo.registry.ApacheDubboRegistryInstanceMethodInterceptor";

    private static final String ENHANCE_METHOD_NAME = "notify";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(REGISTRY_ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(REGISTRY_INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>named(ENHANCE_METHOD_NAME))};
    }
}
