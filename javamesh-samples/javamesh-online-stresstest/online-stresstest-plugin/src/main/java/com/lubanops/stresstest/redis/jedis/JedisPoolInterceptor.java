/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.redis.jedis;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
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
    private static final Logger LOGGER = LogFactory.getLogger();

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
