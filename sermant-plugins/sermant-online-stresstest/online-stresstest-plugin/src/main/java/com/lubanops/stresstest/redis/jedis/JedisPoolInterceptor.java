/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.redis.jedis;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Reflection;
import com.lubanops.stresstest.core.Tester;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Jedis pool 拦截器，返回影子redis 连接。
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class JedisPoolInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        if (!Tester.isTest() || !ConfigFactory.getConfig().isRedisShadowRepositories()) {
            return result;
        }
        if (ShadowJedisFactory.getInstance().isShadowClient(result)) {
            return result;
        }
        Object shadowPool = ShadowJedisFactory.getInstance().getShadowPool(obj);
        if (shadowPool == null) {
            return result;
        }
        try {
            Object shadowJedis = method.invoke(shadowPool);
            Reflection.invokeDeclared("close", result);
            return shadowJedis;
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.severe("Cannot new shadow jedis from pool.");
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
