/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.redisson.v3;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;

/**
 * redisson拦截点
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-05-07
 */
public class RedissonInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "org.redisson.command.CommandAsyncService";

    private static final String REDISSON_METHOD_INTERCEPTOR_CLASS =
            "com.lubanops.apm.plugin.flowrecord.plugins.redisson.v3.RedissonInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(REDISSON_METHOD_INTERCEPTOR_CLASS,
                        RedissonMethodMatch.INSTANCE.getMethodMatcher())
        };
    }
}