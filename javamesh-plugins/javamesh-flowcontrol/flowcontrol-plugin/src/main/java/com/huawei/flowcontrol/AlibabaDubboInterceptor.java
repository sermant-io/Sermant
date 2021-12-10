/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/plugin/dubbo/DubboInterceptor.java
 * from the Apache Skywalking project.
 */

package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcResult;
import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.javamesh.core.agent.common.BeforeResult;

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
            // 流控异常返回上游
            result.setResult(new RpcResult(ex.toRuntimeException()));
            handleBlockException(ex, getResourceName(invoker.getInterface().getName(), invocation.getMethodName()),
                    "AlibabaDubboInterceptor consumer", EntryFacade.DubboType.ALIBABA);
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
