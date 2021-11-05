/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.gray.feign.context.CurrentInstance;
import com.huawei.gray.feign.util.RouterUtil;

import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;

import java.lang.reflect.Method;

/**
 * 拦截zookeeper注册服务的方法，获取当前服务信息
 *
 * @author lilai
 * @since 2021-11-03
 */
public class ZookeeperRegisterInterceptor implements InstanceMethodInterceptor {

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
        if (argument instanceof ZookeeperRegistration) {
            ZookeeperRegistration instanceInfo = (ZookeeperRegistration) argument;
            // 存放当前服务实例, 仅初始化一次
            CurrentInstance.newInstance(instanceInfo.getServiceId(), instanceInfo.getHost(), instanceInfo.getPort());
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
