/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.stresstest.redis.redisson.workaround;

import static com.huawei.sermant.stresstest.redis.redisson.RedissonUtils.buildShadowObject;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Redisson Queue 拦截器
 *
 * @author yiwei
 * @since 2021-11-03
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
                LOGGER.severe(String.format(Locale.ROOT, "Cannot execute method %s for reason %s.", method.getName(),
                    e.getMessage()));
            }
        });
        return result;
    }
}
