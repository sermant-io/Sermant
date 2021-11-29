/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.feign.service;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.gray.feign.context.HostContext;

import feign.Request;

import java.lang.reflect.Method;
import java.net.URI;

/**
 * LoadBalancerClientInterceptor的service
 *
 * @author pengyuyi
 * @date 2021/11/26
 */
public class LoadBalancerClientServiceImpl implements LoadBalancerClientService {
    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

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
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        // 移除线程变量
        HostContext.remove();
    }
}
