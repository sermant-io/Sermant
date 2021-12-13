/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.redis.lettuce;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.redis.RedisUtils;
import com.lubanops.stresstest.redis.lettuce.pool.GenericObjectPoolProxy;
import com.lubanops.stresstest.redis.lettuce.pool.SoftObjectPoolProxy;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;

import java.lang.reflect.Method;

import static com.lubanops.stresstest.redis.lettuce.LettucePoolEnhance.GENERIC_OBJECT_POOL_METHOD;
import static com.lubanops.stresstest.redis.lettuce.LettucePoolEnhance.SOFT_OBJECT_POOL_METHOD;

/**
 * Jedis pool 拦截器，返回影子redis 连接。
 *
 * @author yiwei
 * @since 2021/10/21
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
                return newInstance((GenericObjectPoolConfig<StatefulRedisConnection<?, ?>>) arguments[1],
                        (GenericObjectPool<StatefulRedisConnection<?, ?>>) result);
            }
            if (name.equals(SOFT_OBJECT_POOL_METHOD)) {
                if (result instanceof SoftObjectPoolProxy) {
                    return result;
                }
                return newInstance((SoftReferenceObjectPool<StatefulRedisConnection<?, ?>>) result);
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

    private SoftObjectPoolProxy<StatefulRedisConnection<?, ?>> newInstance(
            SoftReferenceObjectPool<StatefulRedisConnection<?, ?>> pool) {
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
