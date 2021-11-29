/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.flowcontrol.entry.EntryFacade;

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
                    result, "AlibabaDubboInterceptor consumer", EntryFacade.DubboType.ALIBABA);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] allArguments, Object ret) {
        Result result = (Result) ret;
        // 记录dubbo的exception
        if (result != null && result.hasException()) {
            EntryFacade.INSTANCE.tryTraceEntry(result.getException(), RpcContext.getContext().isProviderSide(),
                    EntryFacade.DubboType.ALIBABA);
        }
        EntryFacade.INSTANCE.exit(EntryFacade.DubboType.ALIBABA);
        return ret;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        if (t != null) {
            EntryFacade.INSTANCE.tryTraceEntry(t, RpcContext.getContext().isProviderSide(), EntryFacade.DubboType.ALIBABA);
        }
        EntryFacade.INSTANCE.exit(EntryFacade.DubboType.ALIBABA);
    }
}
