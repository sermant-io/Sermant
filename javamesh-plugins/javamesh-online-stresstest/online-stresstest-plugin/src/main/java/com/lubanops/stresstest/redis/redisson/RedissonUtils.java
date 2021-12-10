/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Reflection;
import com.lubanops.stresstest.redis.redisson.config.ShadowConfigChains;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * redisson 影子相关的工具库
 *
 * @author yiwei
 * @since 2021/11/4
 */
public class RedissonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final String CONNECTION_MANAGER = "org.redisson.connection.ConnectionManager";
    private static final String ASYNC_EXECUTOR = "org.redisson.command.CommandAsyncExecutor";
    private static final String REDISSON_CLIENT = "org.redisson.api.RedissonClient";
    private static final String REDISSON_MAP_CACHE = "org.redisson.RedissonMapCache";

    private static final int OLD_REDISSON_COUNT = 2;

    private static final int NEW_REDISSON_COUNT = 3;

    private static final int NEW_REDISSON_MAP_COUNT = 5;

    private static final int REDISSON_LOC = 2;
    private static ReentrantLock lock = new ReentrantLock();
    private static volatile Object connectionManager;

    /**
     * 根据原始的redisson object，修改key创建对应的redisson object
     * @param object 原始的object
     * @return 影子redisson object
     */
    public static Optional<Object> buildShadowObject(Object object) {
        return Reflection.invokeDeclared("getName", object).map(name -> {
            if (name.toString().startsWith(ConfigFactory.getConfig().getTestRedisPrefix())) {
                return null;
            }
            String shadowName;
            if (!ConfigFactory.getConfig().isRedisShadowRepositories()) {
                shadowName = ConfigFactory.getConfig().getTestRedisPrefix() + name;
            } else {
                shadowName = name.toString();
            }
            return Reflection.getDeclaredValue("commandExecutor", object).map(executor -> {
                Constructor<?>[] constructors = executor.getClass().getDeclaredConstructors();
                for (Constructor<?> constructor : constructors) {
                    Class<?>[] types = constructor.getParameterTypes();
                    if (types.length == 1 && types[0].getName().equals(CONNECTION_MANAGER)) {
                        Reflection.accessible(constructor);
                        return Reflection.getDeclaredValue("connectionManager",
                                executor).map(connectionManager -> {
                                    Optional<Boolean> shadowObj = Reflection.invokeDeclared("getCfg", connectionManager).map(ShadowConfigChains::isShadowObject);
                                    if (shadowObj.isPresent() && shadowObj.get()) {
                                        return null;
                                    }
                                    try {
                                        Object commandExecutor = constructor.newInstance(getSetShadowConnectionManager(connectionManager));
                                        return newShadowNewObject(object, commandExecutor, shadowName,
                                                getClientFromCommandExecutor(executor)).orElseGet(
                                                () -> newShadowOldObject(object, commandExecutor, shadowName).orElse(null));
                                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                        LOGGER.info(String.format("Cannot build constructor, %s.", executor.getClass().getName()));
                                    }
                                    return null;
                                }).orElse(null);
                    }
                }
                return null;
            }).orElse(null);
        });
    }

    private static Optional<Object> newShadowNewObject(Object object, Object commandExecutor, String name,
                Object redisson) {
        Class<?> clazz = object.getClass();
        if (clazz.getName().equals(REDISSON_MAP_CACHE)) {
            return newShadowMapCache(object, clazz, commandExecutor, name, redisson);
        } else {
            try {
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                for (Constructor<?> constructor : constructors) {
                    Class<?>[] types = constructor.getParameterTypes();
                    if (types.length >= NEW_REDISSON_COUNT && isExecutor(types[0]) && types[1] == String.class
                            && types[REDISSON_LOC].getName().equals(REDISSON_CLIENT)) {
                        Reflection.accessible(constructor);
                        Object result;
                        if (types.length == NEW_REDISSON_COUNT) {
                            result = constructor.newInstance(commandExecutor, name, redisson);
                        } else if (types.length == NEW_REDISSON_MAP_COUNT) {
                            result = constructor.newInstance(commandExecutor, name, redisson, null, null);
                        } else {
                            continue;
                        }
                        return Optional.of(result);
                    }
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                LOGGER.info(String.format("Cannot build constructor %s.", clazz.getName()));
            }
            return Optional.empty();
        }
    }

    private static Optional<Object> newShadowOldObject(Object object, Object commandExecutor, String name) {
        Class<?> clazz = object.getClass();
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] types = constructor.getParameterTypes();
                if (types.length == OLD_REDISSON_COUNT && isExecutor(types[0])
                        && types[1] == String.class) {
                    Reflection.accessible(constructor);
                    Object result = constructor.newInstance(commandExecutor, name);
                    return Optional.of(result);
                }
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOGGER.info(String.format("Cannot build constructor %s.", clazz.getName()));
        }
        return Optional.empty();
    }

    private static Optional<Object> newShadowMapCache(
            Object original, Class<?> clazz, Object commandExecutor, String name, Object redisson) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 6 && isExecutor(types[1]) && types[2] == String.class && types[3].getName()
                    .equals(REDISSON_CLIENT)) {
                Reflection.accessible(constructor);
                return Reflection.getDeclaredValue("evictionScheduler", original).map(evictionScheduler -> {
                    Object result = null;
                    try {
                        result = constructor.newInstance(evictionScheduler, commandExecutor, name, redisson,
                                null, null);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        LOGGER.info(String.format("Cannot build constructor %s.", clazz.getName()));
                    }
                    return result;
                });
            }
        }
        return Optional.empty();
    }

    private static boolean isExecutor(Class<?> clazz) {
        if (clazz.getName().equals(ASYNC_EXECUTOR)) {
            return true;
        } else {
            Class<?>[] subClasses = clazz.getInterfaces();
            for (Class<?> subClazz:subClasses) {
                if (isExecutor(subClazz)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从commandexecutor中获取redisson client
     * @param commandExecutor commandexecutor
     * @return redisson client
     */
    private static Object getClientFromCommandExecutor(Object commandExecutor) {
        return Reflection.getDeclaredValue("redisson", commandExecutor).orElseGet(
                () -> Reflection.getDeclaredValue("objectBuilder", commandExecutor).flatMap(builder ->
                        Reflection.getDeclaredValue("redisson", builder)).orElse(null));
    }

    /**
     * 获取影子的connectionManager
     *
     * @param localConnectionManager 原始的connectionManager
     * @return 影子的connectionManger
     */
    public static Object getSetShadowConnectionManager(Object localConnectionManager) {
        if (!ConfigFactory.getConfig().isRedisShadowRepositories()) {
            return localConnectionManager;
        }
        if (connectionManager == null) {
            lock.lock();
            try {
                if (connectionManager == null) {
                    Reflection.invokeDeclared("getCfg", localConnectionManager)
                            .flatMap(RedissonUtils::createConnectionManager)
                            .ifPresent(shadowManager -> connectionManager = shadowManager);
                }
            } finally {
                lock.unlock();
            }
        }
        if (connectionManager != null) {
            return connectionManager;
        } else {
            LOGGER.warning("Use original redisson client");
            return localConnectionManager;
        }
    }

    static Optional<Object> createConnectionManager(Object config) {
        return shadowConfig(config).map(shadowConfig -> Reflection.invokeStaticDeclared("createConnectionManager",
                "org.redisson.config.ConfigSupport", shadowConfig).orElse(config));
    }

    /**
     * 创建redisson影子config
     *
     * @param config 原始的config
     * @return 影子config
     */
    static Optional<Object> shadowConfig(Object config) {
        Class<?> clazz = config.getClass();
        try {
            Constructor<?> constructor = clazz.getConstructor(clazz);
            Object shadowConfig = constructor.newInstance(config);
            ShadowConfigChains.update(shadowConfig);
            return Optional.of(shadowConfig);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                | InvocationTargetException e) {
            LOGGER.severe("Cannot new shadow config.");
        }
        return Optional.empty();
    }
}
