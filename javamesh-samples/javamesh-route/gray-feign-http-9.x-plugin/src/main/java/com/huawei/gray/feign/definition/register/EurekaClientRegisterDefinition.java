/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.definition.register;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截eureka注册中心方法，获取当前服务名
 *
 * @author lilai
 * @since 2021-11-03
 */
public class EurekaClientRegisterDefinition implements EnhanceDefinition {

    /**
     * Intercept class.
     */
    private static final String INTERCEPT_CLASS = "com.huawei.gray.feign.interceptor.EurekaClientRegisterInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass("com.netflix.discovery.shared.transport.decorator.EurekaHttpClientDecorator",
                "org.springframework.cloud.netflix.eureka.http.RestTemplateEurekaHttpClient",
                "org.springframework.cloud.netflix.eureka.http.WebClientEurekaHttpClient",
                "com.netflix.discovery.shared.transport.jersey.AbstractJerseyEurekaHttpClient");
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(
                        INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named("register")
                )
        };
    }
}
