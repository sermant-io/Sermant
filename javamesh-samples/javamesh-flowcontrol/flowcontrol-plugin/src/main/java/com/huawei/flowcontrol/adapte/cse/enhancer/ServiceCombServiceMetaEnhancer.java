/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.enhancer;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.servicecomb.governance.MicroserviceMeta;

/**
 * 拦截servicecomb的服务名与版本
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class ServiceCombServiceMetaEnhancer implements EnhanceDefinition {
    /**
     * 拦截器
     */
    private static final String INTERCEPTOR_CLASS = "com.huawei.flowcontrol.adapte.cse.interceptors.MetricServiceMetaInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.hasSuperTypes(MicroserviceMeta.class);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[] {
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPTOR_CLASS,
                        ElementMatchers.<MethodDescription>named("getName")
                                .or(ElementMatchers.<MethodDescription>named("getVersion")))
        };
    }
}
