/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.interceptor.apache;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.service.ServiceManager;
import com.huawei.gray.dubbo.service.RegistryDirectoryService;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强RegistryDirectory类的notify方法，获取并维护应用缓存的路由信息
 *
 * @author pengyuyi
 * @since 2021年6月28日
 */
public class RegistryDirectoryInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private RegistryDirectoryService registryDirectoryService;

    /**
     * Dubbo每次与注册中心交互之后，同步缓存的URL信息
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param beforeResult the method's original return value. May be null if the method triggers an exception.
     * @throws Exception 增强时可能出现的异常
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        registryDirectoryService = ServiceManager.getService(RegistryDirectoryService.class);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        registryDirectoryService.after(obj, method, arguments, result);
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "RegistryDirectory is error!", throwable);
    }
}