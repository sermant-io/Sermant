/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.nacos;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * nacos拦截
 *
 * @author zhouss
 * @since 2021-10-30
 */
public class NacosNamespaceDefinition implements EnhanceDefinition {
    /**
     * Enhance class.
     */
    private static final String ENHANCE_CLASS = "com.alibaba.nacos.client.naming.utils.InitUtils";

    /**
     * 命名空间拦截器
     */
    private static final String NAMESPACE_INTERCEPT_CLASS = "com.huawei.route.nacos.NacosNamespaceInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[] {
                MethodInterceptPoint.newStaticMethodInterceptPoint(NAMESPACE_INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>named("initNamespaceForNaming"))
        };
    }
}
