/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.redis.jedis;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Tester;
import com.lubanops.stresstest.redis.jedis.command.HandlerFactory;

import java.lang.reflect.Method;

/**
 * Jedis Cluster 拦截器，修改getSlot方法。
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class JedisKeyInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (Tester.isTest() && !ConfigFactory.getConfig().isRedisShadowRepositories() && arguments.length > 1 &&
                arguments[1] instanceof byte[][]) {
            arguments[1] = HandlerFactory.getHandler(arguments[0]).handle((byte[][])arguments[1]);
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
