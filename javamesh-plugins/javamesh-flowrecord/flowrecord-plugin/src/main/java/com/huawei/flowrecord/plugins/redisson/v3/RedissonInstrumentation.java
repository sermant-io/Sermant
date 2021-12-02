/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.plugins.redisson.v3;

import com.huawei.javamesh.core.agent.definition.EnhanceDefinition;
import com.huawei.javamesh.core.agent.definition.MethodInterceptPoint;
import com.huawei.javamesh.core.agent.matcher.ClassMatcher;
import com.huawei.javamesh.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * redisson拦截点
 *
 */
public class RedissonInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "org.redisson.command.CommandAsyncService";

    private static final String REDISSON_METHOD_INTERCEPTOR_CLASS =
            "com.huawei.flowrecord.plugins.redisson.v3.RedissonInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(REDISSON_METHOD_INTERCEPTOR_CLASS,
                        ElementMatchers.namedOneOf("writeAsync", "readAsync", "evalWriteAllAsync", "writeAllAsync",
                                "readAllAsync", "evalReadAsync", "evalWriteAsync", "readRandomAsync", "pollFromAnyAsync",
                                "readBatchedAsync", "writeBatchedAsync"))
        };
    }
}