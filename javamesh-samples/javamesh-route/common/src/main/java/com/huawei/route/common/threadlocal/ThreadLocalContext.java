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
@SuppressWarnings("all")
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

    public void put(String key, Object val) {
        context.put(key, val);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public <T> T get(String key, Class<T> tClass) {
        return (T) context.get(key);
    }

    public void remove(String key) {
        context.remove(key);
        if (context.isEmpty()) {
            threadLocal.remove();
        }
    }
}
