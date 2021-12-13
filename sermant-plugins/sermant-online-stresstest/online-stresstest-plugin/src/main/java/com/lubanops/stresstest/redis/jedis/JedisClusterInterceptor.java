/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.redis.jedis;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Tester;
import com.lubanops.stresstest.redis.RedisUtils;

import java.lang.reflect.Method;

/**
 * Jedis Cluster 拦截器，修改getSlot方法。
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class JedisClusterInterceptor implements StaticMethodInterceptor {
    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (Tester.isTest() && !ConfigFactory.getConfig().isRedisShadowRepositories() && arguments.length > 0 ) {
            arguments[0] = RedisUtils.modifySingleKey(arguments[0]);
        }
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable throwable) {
    }
}
