/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.javamesh.core.service.ServiceManager;
import com.huawei.gray.feign.service.LoadBalancerClientService;

import java.lang.reflect.Method;

/**
 * 拦截LoadBalancerFeignClientInstrumentation的execute方法，获取request的域名host（服务名称）
 *
 * @author lilai
 * @since 2021-11-03
 */
public class LoadBalancerClientInterceptor implements InstanceMethodInterceptor {
    private LoadBalancerClientService loadBalancerClientService;

    /**
     * 拦截获取下游服务名称，并存放到线程变量中
     *
     * @param obj          拦截对象
     * @param method       拦截方法
     * @param arguments    方法参数
     * @param beforeResult change this result, if you want to truncate the method.
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        loadBalancerClientService = ServiceManager.getService(LoadBalancerClientService.class);
        loadBalancerClientService.before(obj, method, arguments, beforeResult);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        loadBalancerClientService.onThrow(obj, method, arguments, t);
    }
}
