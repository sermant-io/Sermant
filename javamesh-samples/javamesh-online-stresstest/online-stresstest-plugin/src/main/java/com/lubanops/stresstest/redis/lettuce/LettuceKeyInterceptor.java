/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.redis.lettuce;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Tester;
import com.lubanops.stresstest.redis.RedisUtils;

import java.lang.reflect.Method;

/**
 * Lettuce key 拦截器
 *
 * @author yiwei
 * @since 2021/11/2
 */
public class LettuceKeyInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (Tester.isTest() && !ConfigFactory.getConfig().isRedisShadowRepositories() && arguments.length > 0) {
            arguments[0] = RedisUtils.modifyKey(arguments[0]);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
