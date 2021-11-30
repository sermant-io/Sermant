/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.connection.pool.collect.definition;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Druid Monitor 增强定义
 */
public class DruidDataSourceStatManagerDefinition implements EnhanceDefinition {

    private static final String ENHANCE_CLASS = "com.alibaba.druid.stat.DruidDataSourceStatManager";
    private static final String ADD_DATA_SOURCE_INTERCEPTOR =
        "com.huawei.javamesh.sample.connection.pool.collect.interceptor.AddDataSourceInterceptor";
    private static final String REMOVE_DATA_SOURCE_INTERCEPTOR =
        "com.huawei.javamesh.sample.connection.pool.collect.interceptor.RemoveDataSourceInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
            MethodInterceptPoint.newStaticMethodInterceptPoint(ADD_DATA_SOURCE_INTERCEPTOR,
                ElementMatchers.<MethodDescription>named("addDataSource")),
            MethodInterceptPoint.newStaticMethodInterceptPoint(REMOVE_DATA_SOURCE_INTERCEPTOR,
                ElementMatchers.<MethodDescription>named("removeDataSource"))
        };
    }
}
