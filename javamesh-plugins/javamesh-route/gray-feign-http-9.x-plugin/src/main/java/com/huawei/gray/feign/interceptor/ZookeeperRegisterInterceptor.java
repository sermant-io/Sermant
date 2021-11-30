/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.javamesh.core.service.ServiceManager;
import com.huawei.gray.feign.service.RegisterService;

import java.lang.reflect.Method;

/**
 * 拦截zookeeper注册服务的方法，获取当前服务信息
 *
 * @author lilai
 * @since 2021-11-03
 */
public class ZookeeperRegisterInterceptor implements InstanceMethodInterceptor {
    private RegisterService registerService;

    /**
     * 获取当前服务信息
     *
     * @param obj          拦截对象
     * @param method       拦截方法
     * @param arguments    方法参数
     * @param beforeResult change this result, if you want to truncate the method.
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        registerService = ServiceManager.getService(RegisterService.class);
        registerService.before(obj, method, arguments, beforeResult);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
    }
}
