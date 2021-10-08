/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.redisson.v3;

import static net.bytebuddy.matcher.ElementMatchers.named;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * redisson拦截方法
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-05-07
 */
public enum RedissonMethodMatch {
    /**
     * 实例
     */
    INSTANCE;

    public ElementMatcher<MethodDescription> getMethodMatcher() {
        return named("writeAsync")
            .or(named("readAsync"))
            .or(named("evalWriteAllAsync"))
            .or(named("writeAllAsync"))
            .or(named("readAllAsync"))
            .or(named("evalReadAsync"))
            .or(named("evalWriteAsync"))
            .or(named("readRandomAsync"))
            .or(named("pollFromAnyAsync"))
            .or(named("readBatchedAsync"))
            .or(named("writeBatchedAsync"));
    }
}
