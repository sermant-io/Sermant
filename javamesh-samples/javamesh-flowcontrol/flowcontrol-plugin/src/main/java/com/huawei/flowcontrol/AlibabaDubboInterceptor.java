/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.huawei.apm.bootstrap.common.BeforeResult;
import org.apache.dubbo.rpc.Result;

import java.lang.reflect.Method;

/**
 * alibaba dubbo拦截后的增强类
 * 埋点定义sentinel资源
 *
 * @author liyi
 * @since 2020-08-26
 */
public class AlibabaDubboInterceptor extends DubboInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] allArguments, BeforeResult result) {
        Invoker invoker = null;
        if (allArguments[0] instanceof Invoker) {
            invoker = (Invoker) allArguments[0];
        }
        Invocation invocation = null;
        if (allArguments[1] instanceof Invocation) {
            invocation = (Invocation) allArguments[1];
        }
        if (invocation == null || invoker == null) {
            return;
        }
        RpcContext rpcContext = RpcContext.getContext();
        entry(rpcContext.isConsumerSide(), invoker.getInterface().getName(), invocation.getMethodName(), result);
    }

    @Override
    public Object after(Object obj, Method method, Object[] allArguments, Object ret) {
        Result result = (Result) ret;

        // 记录dubbo的exception
        if (result != null && result.hasException()) {
            handleException(result.getException());
        }
        removeThreadLocalEntry();
        return ret;
    }

}
