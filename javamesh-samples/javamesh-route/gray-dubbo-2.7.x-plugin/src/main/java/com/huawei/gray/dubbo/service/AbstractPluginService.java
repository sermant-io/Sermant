/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.service;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.plugin.service.PluginService;

import java.lang.reflect.Method;

/**
 * 插件服务基类
 *
 * @author pengyuyi
 * @date 2021/11/24
 */
public abstract class AbstractPluginService implements PluginService {
    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    /**
     * 拦截点前执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param beforeResult 执行结果承载类
     * @throws Exception 增强时可能出现的异常
     */
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
    }

    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param result the method's original return value. May be null if the method triggers an exception.
     * @throws Exception 增强时可能出现的异常
     */
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
    }

    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param throwable 增强时可能出现的异常
     */
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}