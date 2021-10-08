/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.boot;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.lubanops.apm.plugin.flowrecord.init.InitListener;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

/**
 * 启动类增强逻辑
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */
public class BootInterceptor implements StaticMethodInterceptor {
    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                try {
                    InitListener.doinit();
                } catch (Exception e) {
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
