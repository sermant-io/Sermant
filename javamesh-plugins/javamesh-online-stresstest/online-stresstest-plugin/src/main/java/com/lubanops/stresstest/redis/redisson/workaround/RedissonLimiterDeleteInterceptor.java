/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson.workaround;


import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;

import java.lang.reflect.Method;

import static com.lubanops.stresstest.redis.redisson.workaround.RedissonShadowInterceptor.invokeOnShadow;

/**
 * 影子RedissonLimiter 拦截器
 *
 * @author yiwei
 * @since 2021/11/4
 */
public class RedissonLimiterDeleteInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        if (obj.getClass().getName().equals("org.redisson.RedissonRateLimiter")) {
            return invokeOnShadow(obj, method, arguments, result);
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}

