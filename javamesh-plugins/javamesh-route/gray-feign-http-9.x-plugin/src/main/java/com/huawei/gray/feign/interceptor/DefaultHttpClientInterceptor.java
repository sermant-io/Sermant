/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.javamesh.core.service.ServiceManager;
import com.huawei.gray.feign.service.DefaultHttpClientService;

import java.lang.reflect.Method;

/**
 * 拦截feign执行http请求的execute方法，匹配标签规则进行灰度路由
 *
 * @author lilai
 * @since 2021-11-03
 */
public class DefaultHttpClientInterceptor implements InstanceMethodInterceptor {
    private DefaultHttpClientService defaultHttpClientService;

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
        defaultHttpClientService = ServiceManager.getService(DefaultHttpClientService.class);
        defaultHttpClientService.before(obj, method, arguments, beforeResult);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        defaultHttpClientService.after(obj, method, arguments, result);
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        defaultHttpClientService.onThrow(obj, method, arguments, t);
    }
}
