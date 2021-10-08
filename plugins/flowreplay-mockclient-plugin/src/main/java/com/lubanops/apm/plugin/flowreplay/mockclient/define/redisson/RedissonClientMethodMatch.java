/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.redisson;

import static net.bytebuddy.matcher.ElementMatchers.named;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 匹配Redisson中的方法
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-06
 */
public enum RedissonClientMethodMatch {
    /**
     * 新建实例
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
