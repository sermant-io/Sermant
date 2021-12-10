/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson.workaround;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import static com.lubanops.stresstest.redis.redisson.RedissonUtils.buildShadowObject;

/**
 * Redisson Queue 拦截器
 *
 * @author yiwei
 * @since 2021/11/3
 */
public class RedissonShadowInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return invokeOnShadow(obj, method, arguments, result);
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }

    /**
     * 在影子对象上调用方法
     *
     * @param obj 原始的对象
     * @param method 待执行的方法
     * @param arguments 方法的参数
     * @param result 返回值
     * @return 原始的返回值。
     */
    public static Object invokeOnShadow(Object obj, Method method, Object[] arguments, Object result) {
        buildShadowObject(obj).ifPresent(shadowInstance -> {
            try {
                method.invoke(shadowInstance, arguments);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.severe(String.format("Cannot execute method %s for reason %s.", method.getName(),
                        e.getMessage()));
            }
        });
        return result;
    }
}
