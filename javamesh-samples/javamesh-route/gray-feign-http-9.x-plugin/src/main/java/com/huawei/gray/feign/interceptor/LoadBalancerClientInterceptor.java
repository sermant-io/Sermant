/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.gray.feign.context.HostContext;

import feign.Request;

import java.lang.reflect.Method;
import java.net.URI;

/**
 * 拦截LoadBalancerFeignClientInstrumentation的execute方法，获取request的域名host（服务名称）
 *
 * @author lilai
 * @since 2021-11-03
 */
public class LoadBalancerClientInterceptor implements InstanceMethodInterceptor {

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
        final Object argument = arguments[0];
        if (argument instanceof Request) {
            Request request = (Request) argument;
            URI uri = URI.create(request.url());
            // 将下游服务名存入线程变量中
            HostContext.set(uri.getHost());
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        // 移除线程变量
        HostContext.remove();
    }
}
