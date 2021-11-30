/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.flowcontrol.entry.EntryFacade;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;

import java.lang.reflect.Method;

/**
 * apache dubbo拦截后的增强类,埋点定义sentinel资源
 *
 * @author liyi
 * @since 2020-08-26
 */
public class ApacheDubboInterceptor extends DubboInterceptor  {
    @Override
    public void before(Object obj, Method method, Object[] allArguments, BeforeResult result) {
        Invoker<?> invoker = null;
        if (allArguments[0] instanceof Invoker) {
            invoker = (Invoker<?>) allArguments[0];
        }
        Invocation invocation = null;
        if (allArguments[1] instanceof Invocation) {
            invocation = (Invocation) allArguments[1];
        }
        if (invocation == null || invoker == null) {
            return;
        }
        try {
            EntryFacade.INSTANCE.tryEntry(invocation);
        } catch (BlockException ex) {
            handleBlockException(ex, getResourceName(invoker.getInterface().getName(), invocation.getMethodName()),
                    result, "ApacheDubboInterceptor consumer", EntryFacade.DubboType.APACHE);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] allArguments, Object ret) {
        Result result = (Result) ret;
        // 记录dubbo的exception
        if (result != null && result.hasException()) {
            EntryFacade.INSTANCE.tryTraceEntry(result.getException(), RpcContext.getContext().isProviderSide(),
                    EntryFacade.DubboType.APACHE);
        }
        EntryFacade.INSTANCE.exit(EntryFacade.DubboType.APACHE);
        return ret;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        if (t != null) {
            EntryFacade.INSTANCE.tryTraceEntry(t, RpcContext.getContext().isProviderSide(), EntryFacade.DubboType.APACHE);
        }
        EntryFacade.INSTANCE.exit(EntryFacade.DubboType.APACHE);
    }
}
