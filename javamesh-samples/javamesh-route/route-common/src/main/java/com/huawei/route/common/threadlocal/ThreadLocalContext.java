/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.threadlocal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程变量上下文
 * 基于MAP管理多个键值
 *
 * @author zhouss
 * @since 2021-11-03
 */
public enum ThreadLocalContext {
    /**
     * 线程上下文变量单例
     */
    INSTANCE;

    /**
     * 存储上下文
     */
    private final Map<String, Object> context = new ConcurrentHashMap<String, Object>();

    private final ThreadLocal<ThreadLocalContext> threadLocal = new ThreadLocal<ThreadLocalContext>();

    ThreadLocalContext() {
        threadLocal.set(this);
    }

    /**
     * 存放变量
     *
     * @param key 存放键
     * @param val 存放值
     */
    public void put(String key, Object val) {
        context.put(key, val);
    }

    /**
     * 获取变量
     * @param key 存放键
     * @return Object
     */
    public Object get(String key) {
        return context.get(key);
    }

    /**
     * 获取变量
     *
     * @param key 键
     * @param tClass 目标类型
     * @param <T> 目标类型
     * @return 值
     */
    public <T> T get(String key, Class<T> tClass) {
        return (T) context.get(key);
    }

    /**
     * 移除变量
     *
     * @param key 键
     */
    public void remove(String key) {
        context.remove(key);
        if (context.isEmpty()) {
            threadLocal.remove();
        }
    }
}
