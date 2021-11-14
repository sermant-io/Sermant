/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.interceptor;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * 日志功能的拦截器示例，本示例将展示如何在插件端使用日志功能
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoLoggerInterceptor implements StaticMethodInterceptor {
    private final Logger logger = LogFactory.getLogger();

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        logger.info(clazz.getSimpleName() + ": [DemoLoggerInterceptor]-before");
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        logger.info(clazz.getSimpleName() + ": [DemoLoggerInterceptor]-after");
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {
        logger.warning(clazz.getSimpleName() + ": [DemoLoggerInterceptor]-onThrow");
    }
}
