/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.redis.jedis;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.lubanops.stresstest.core.Reflection;
import com.lubanops.stresstest.redis.RedisUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * 影子Jedis 工厂
 *
 * @author yiwei
 * @since 2021/11/4
 */
public class ShadowJedisFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private final String shadowHostAndPort;
    private volatile Object shadowPool;
    private final ReentrantLock lock;

    private static final ShadowJedisFactory INSTANCE = new ShadowJedisFactory();

    private ShadowJedisFactory() {
        shadowHostAndPort = RedisUtils.getJedisAddress();
        lock = new ReentrantLock();
    }

    /**
     * 单例模式
     *
     * @return 影子jedis factory
     */
    public static ShadowJedisFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 获取影子的 jedis pool
     *
     * @param jedisPool 原始的jedis pool
     * @return 影子jedis pool
     */
    public Object getShadowPool(Object jedisPool) {
        if (shadowPool == null) {
            lock.lock();
            try {
                if (shadowPool == null) {
                    shadowPool = getSetShadowJedisPool(jedisPool).orElse(null);
                }
            } finally {
                lock.unlock();
            }
        }
        return shadowPool;
    }

    /**
     * 查询当前的jedis是否是影子的redis
     *
     * @param jedis 待检查的redis
     * @return 如果是影子redis返回true，否则返回false
     */
    public boolean isShadowClient(Object jedis) {
        StringBuilder builder = new StringBuilder();
        if (jedis == null) {
            return true;
        }
        Reflection.invokeDeclared("getClient", jedis).ifPresent(client -> {
            builder.append(Reflection.invokeDeclared("getHost", client).orElse(""));
            builder.append(":");
            builder.append(Reflection.invokeDeclared("getPort", client).orElse(""));
        });
        return shadowHostAndPort.contains(builder.toString());
    }

    private Optional<Object> getSetShadowJedisPool(Object jedisPool) {
        Class<?> clazz = jedisPool.getClass();
        try {
            final Object shadowJedisPool = clazz.newInstance();
            Reflection.getDeclaredValue("internalPool", shadowJedisPool)
                    .ifPresent(internalShadowPool -> Reflection.getDeclaredValue("internalPool", jedisPool)
                            .ifPresent(internalPool ->
                                    setInternalShadowPool(internalPool, internalShadowPool, shadowHostAndPort)));
            return Optional.of(shadowJedisPool);
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.severe("Cannot new shadow jedis pool.");
        }
        return Optional.empty();
    }

    private void setInternalShadowPool(Object internalPool, Object internalShadowPool, String shadowHostAndPort) {
        Reflection.getDeclaredValue("factory", internalPool).ifPresent(factory ->
                Reflection.getDeclaredValue("factory", internalShadowPool).ifPresent(shadowFactory -> {
                    setShadowFactory(factory, shadowFactory, shadowHostAndPort);
                    copyPool(internalPool, internalShadowPool);
                }));
    }

    private void setShadowFactory(Object factory, Object shadowFactory, String shadowHostAndPort) {
        Reflection.getDeclaredValue("connectionTimeout", factory).ifPresent(value ->
                Reflection.setDeclaredValue("connectionTimeout", shadowFactory, value));

        Reflection.getDeclaredValue("soTimeout", factory).ifPresent(value ->
                Reflection.setDeclaredValue("soTimeout", shadowFactory, value));

        Reflection.getDeclaredValue("password", factory).ifPresent(value ->
                Reflection.setDeclaredValue("password", shadowFactory, value));

        Reflection.getDeclaredValue("database", factory).ifPresent(value ->
                Reflection.setDeclaredValue("database", shadowFactory, value));

        Reflection.getDeclaredValue("clientName", factory).ifPresent(value ->
                Reflection.setDeclaredValue("clientName", shadowFactory, value));

        Reflection.getDeclaredValue("user", factory).ifPresent(value ->
                Reflection.setDeclaredValue("user", shadowFactory, value));

        Reflection.getDeclaredValue("ssl", factory).ifPresent(value ->
                Reflection.setDeclaredValue("ssl", shadowFactory, value));

        Reflection.getDeclaredValue("sslSocketFactory", factory).ifPresent(value ->
                Reflection.setDeclaredValue("sslSocketFactory", shadowFactory, value));

        Reflection.getDeclaredValue("sslParameters", factory).ifPresent(value ->
                Reflection.setDeclaredValue("sslParameters", shadowFactory, value));

        Reflection.getDeclaredValue("hostnameVerifier", factory).ifPresent(value ->
                Reflection.setDeclaredValue("hostnameVerifier", shadowFactory, value));

        Reflection.getDeclaredValue("hostAndPort", shadowFactory).ifPresent(hostAndPort -> {
            if (hostAndPort instanceof AtomicReference) {
                AtomicReference<?> ref = (AtomicReference<?>)hostAndPort;
                Object value = ref.get();
                String[] shadowAddressInfos = shadowHostAndPort.split(":");
                String shadowIp = shadowAddressInfos[0];
                int shadowPort = Integer.parseInt(shadowAddressInfos[1]);
                Reflection.setDeclaredValue("host", value, shadowIp);
                Reflection.setDeclaredValue("port", value, shadowPort);
            }
        });
    }

    /**
     * 从原始的pool 赋值到影子pool
     * @param internalPool 原始的pool
     * @param internalShadowPool 影子pool
     */
    private void copyPool(Object internalPool, Object internalShadowPool) {
        Reflection.invokeDeclared("getMaxIdle", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setMaxIdle", internalShadowPool, value));

        Reflection.invokeDeclared("getMinIdle", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setMinIdle", internalShadowPool, value));

        Reflection.invokeDeclared("getMaxTotal", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setMaxTotal", internalShadowPool, value));

        Reflection.invokeDeclared("getMaxWaitMillis", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setMaxWaitMillis", internalShadowPool, value));

        Reflection.invokeDeclared("getBlockWhenExhausted", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setBlockWhenExhausted", internalShadowPool, value));

        Reflection.invokeDeclared("getFairness", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setFairness", internalShadowPool, value));

        Reflection.invokeDeclared("getLifo", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setLifo", internalShadowPool, value));

        Reflection.invokeDeclared("getTestOnBorrow", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setTestOnBorrow", internalShadowPool, value));

        Reflection.invokeDeclared("getTestOnCreate", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setTestOnCreate", internalShadowPool, value));

        Reflection.invokeDeclared("getTestOnReturn", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setTestOnReturn", internalShadowPool, value));

        Reflection.invokeDeclared("getTestWhileIdle", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setTestWhileIdle", internalShadowPool, value));

        Reflection.invokeDeclared("getNumTestsPerEvictionRun", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setNumTestsPerEvictionRun", internalShadowPool, value));

        Reflection.invokeDeclared("getMinEvictableIdleTimeMillis", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setMinEvictableIdleTimeMillis", internalShadowPool, value));

        Reflection.invokeDeclared("getTimeBetweenEvictionRunsMillis", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setTimeBetweenEvictionRunsMillis", internalShadowPool, value));

        Reflection.invokeDeclared("getSoftMinEvictableIdleTimeMillis", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setSoftMinEvictableIdleTimeMillis", internalShadowPool, value));

        Reflection.invokeDeclared("getEvictionPolicyClassName", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setEvictionPolicyClassName", internalShadowPool, value));

        Reflection.invokeDeclared("getEvictionPolicy", internalPool).ifPresent(value ->
                Reflection.invokeDeclared("setEvictionPolicy", internalShadowPool, value));
    }
}
