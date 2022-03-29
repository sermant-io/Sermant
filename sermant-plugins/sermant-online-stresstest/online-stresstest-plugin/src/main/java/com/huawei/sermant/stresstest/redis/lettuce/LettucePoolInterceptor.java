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

package com.huawei.sermant.stresstest.redis.lettuce;

import static com.huawei.sermant.stresstest.redis.lettuce.LettucePoolEnhance.GENERIC_OBJECT_POOL_METHOD;
import static com.huawei.sermant.stresstest.redis.lettuce.LettucePoolEnhance.SOFT_OBJECT_POOL_METHOD;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.stresstest.config.ConfigFactory;
import com.huawei.sermant.stresstest.redis.RedisUtils;
import com.huawei.sermant.stresstest.redis.lettuce.pool.GenericObjectPoolProxy;
import com.huawei.sermant.stresstest.redis.lettuce.pool.SoftObjectPoolProxy;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;

import java.lang.reflect.Method;

/**
 * Jedis pool 拦截器，返回影子redis 连接。
 *
 * @author yiwei
 * @since 2021-10-21
 */
public class LettucePoolInterceptor implements StaticMethodInterceptor {
    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) {
        if (ConfigFactory.getConfig().isRedisShadowRepositories()) {
            String name = method.getName();
            if (name.equals(GENERIC_OBJECT_POOL_METHOD)) {
                if (result instanceof GenericObjectPoolProxy) {
                    return result;
                }
                return newInstance((GenericObjectPoolConfig<StatefulRedisConnection<?, ?>>)arguments[1],
                    (GenericObjectPool<StatefulRedisConnection<?, ?>>)result);
            }
            if (name.equals(SOFT_OBJECT_POOL_METHOD)) {
                if (result instanceof SoftObjectPoolProxy) {
                    return result;
                }
                return newInstance((SoftReferenceObjectPool<StatefulRedisConnection<?, ?>>)result);
            }
        }
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable throwable) {
    }

    private GenericObjectPoolProxy<StatefulRedisConnection<?, ?>> newInstance(
        GenericObjectPoolConfig<StatefulRedisConnection<?, ?>> config,
        GenericObjectPool<StatefulRedisConnection<?, ?>> pool) {
        RedisClient client = RedisClient.create(RedisUtils.getMasterAddress());
        return new GenericObjectPoolProxy<>(new BasePooledObjectFactory<StatefulRedisConnection<?, ?>>() {
            @Override
            public StatefulRedisConnection<?, ?> create() {
                return client.connect();
            }

            @Override
            public PooledObject<StatefulRedisConnection<?, ?>> wrap(StatefulRedisConnection obj) {
                return new DefaultPooledObject<StatefulRedisConnection<?, ?>>(obj);
            }

            @Override
            public void destroyObject(PooledObject<StatefulRedisConnection<?, ?>> pooledObject) {
                pooledObject.getObject().close();
            }

            @Override
            public boolean validateObject(PooledObject<StatefulRedisConnection<?, ?>> pooledObject) {
                return pooledObject.getObject().isOpen();
            }
        }, config, pool);
    }

    private SoftObjectPoolProxy<StatefulRedisConnection<?, ?>>
        newInstance(SoftReferenceObjectPool<StatefulRedisConnection<?, ?>> pool) {
        RedisClient client = RedisClient.create(RedisUtils.getMasterAddress());
        return new SoftObjectPoolProxy<>(new BasePooledObjectFactory<StatefulRedisConnection<?, ?>>() {
            @Override
            public StatefulRedisConnection<?, ?> create() {
                return client.connect();
            }

            @Override
            public PooledObject<StatefulRedisConnection<?, ?>> wrap(StatefulRedisConnection obj) {
                return new DefaultPooledObject<StatefulRedisConnection<?, ?>>(obj);
            }

            @Override
            public void destroyObject(PooledObject<StatefulRedisConnection<?, ?>> pooledObject) {
                pooledObject.getObject().close();
            }

            @Override
            public boolean validateObject(PooledObject<StatefulRedisConnection<?, ?>> pooledObject) {
                return pooledObject.getObject().isOpen();
            }
        }, pool);
    }
}
