/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.gray.feign.context.CurrentInstance;
import com.huawei.gray.feign.util.RouterUtil;

import com.netflix.appinfo.InstanceInfo;

import java.lang.reflect.Method;

/**
 * 拦截eureka注册服务的方法，获取应用信息
 *
 * @author lilai
 * @since 2021-11-03
 */
public class EurekaClientRegisterInterceptor implements InstanceMethodInterceptor {

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
        final Object argument = arguments[0];
        if (argument instanceof InstanceInfo) {
            InstanceInfo instanceInfo = (InstanceInfo) argument;
            // 存放当前服务实例, 初始化标签观察者
            CurrentInstance.newInstance(instanceInfo.getVIPAddress(), instanceInfo.getIPAddr(), instanceInfo.getPort());
            RouterUtil.init();
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
    }
}
