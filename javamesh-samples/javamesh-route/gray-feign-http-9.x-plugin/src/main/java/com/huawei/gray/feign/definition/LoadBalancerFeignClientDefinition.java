/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.definition;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截FeignBlockingLoadBalancerClient#execute方法
 * 获取下游服务名称，该方法会在client调用之前，且request域名还未进行解析，因此可通过该参数拿到下游的服务名称
 *
 * @author lilai
 * @since 2021-11-03
 */
public class LoadBalancerFeignClientDefinition implements EnhanceDefinition {
    private static final String ENHANCE_CLASS_LOAD_BALANCER =
            "org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient";

    private static final String ENHANCE_CLASS_RETRY_LOAD_BALANCER =
            "org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient";
    /**
     * feign 2.x版本
     */
    private static final String ENHANCE_CLASS_OLD_LOAD_BALANCER =
            "org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient";

    /**
     * Intercept class.
     */
    private static final String INTERCEPT_CLASS = "com.huawei.gray.feign.interceptor.LoadBalancerClientInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass(ENHANCE_CLASS_LOAD_BALANCER, ENHANCE_CLASS_RETRY_LOAD_BALANCER,
                ENHANCE_CLASS_OLD_LOAD_BALANCER);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(
                        INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named("execute")
                )
        };
    }
}

