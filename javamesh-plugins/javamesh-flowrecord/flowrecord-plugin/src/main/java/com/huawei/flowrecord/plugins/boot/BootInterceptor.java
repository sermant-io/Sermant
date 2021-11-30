/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.plugins.boot;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.flowrecord.init.InitListener;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 启动类增强逻辑
 *
 */
public class BootInterceptor implements StaticMethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootInterceptor.class);
    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                try {
                    InitListener.doinit();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }).start();

    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {

    }
}
