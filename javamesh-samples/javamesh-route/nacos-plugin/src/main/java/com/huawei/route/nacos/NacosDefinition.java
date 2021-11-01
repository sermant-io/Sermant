/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.nacos;

import com.alibaba.nacos.api.naming.pojo.Instance;
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
public class NacosDefinition implements EnhanceDefinition {
    /**
     * Enhance class.
     */
    private static final String ENHANCE_CLASS = "com.alibaba.nacos.client.naming.NacosNamingService";

    /**
     * Intercept class.
     */
    private static final String INTERCEPT_CLASS = "com.huawei.route.nacos.NacosInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[] {
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>named("registerInstance")
                                .and(ElementMatchers.takesArgument(2, Instance.class)))
        };
    }
}
