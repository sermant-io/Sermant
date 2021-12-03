/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.connection.pool.collect.interceptor;

import com.alibaba.druid.pool.DruidDataSource;
import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.javamesh.sample.connection.pool.collect.service.DruidMonitorService;

import java.lang.reflect.Method;

/**
 * Druid Monitor 移除数据源拦截器
 */
public class RemoveDataSourceInterceptor implements StaticMethodInterceptor {

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        if (arguments != null && arguments.length > 0) {
            Object dataSource = arguments[0];
            if (dataSource instanceof DruidDataSource) {
                DruidMonitorService.getInstance().removeDataSource((DruidDataSource) dataSource);
            }
        }
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {

    }
}
