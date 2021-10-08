/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.redisson;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;

/**
 * redisson 拦截器
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-06
 */
public class RedissonClientInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "org.redisson.command.CommandAsyncService";

    private static final String INTERCEPT_CLASS =
            "com.lubanops.apm.plugin.flowreplay.mockclient.define.redisson.RedissonClientInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS, RedissonClientMethodMatch.INSTANCE.getMethodMatcher())
        };
    }
}
