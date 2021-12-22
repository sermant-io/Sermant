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

package com.huawei.flowcontrol.service;

import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.flowcontrol.util.DubboUtil;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.util.SpiLoadUtil.SpiWeight;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcResult;

import java.lang.reflect.Method;

@SpiWeight(1)
public class AlibabaDubboServiceImpl extends AlibabaDubboService {
    /**
     * 拦截点前执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param beforeResult 执行结果承载类
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        Invoker<?> invoker = null;
        if (arguments[0] instanceof Invoker) {
            invoker = (Invoker<?>) arguments[0];
        }
        Invocation invocation = null;
        if (arguments[1] instanceof Invocation) {
            invocation = (Invocation) arguments[1];
        }
        if (invocation == null || invoker == null) {
            return;
        }
        try {
            EntryFacade.INSTANCE.tryEntry(invocation);
        } catch (BlockException ex) {
            // 流控异常返回上游
            beforeResult.setResult(new RpcResult(ex.toRuntimeException()));
            DubboUtil.handleBlockException(ex, DubboUtil.getResourceName(invoker.getInterface().getName(),
                    invocation.getMethodName()),
                    "AlibabaDubboInterceptor consumer", EntryFacade.DubboType.ALIBABA);
        }
    }

    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param ret the method's original return value. May be null if the method triggers an exception.
     */
    @Override
    public void after(Object obj, Method method, Object[] arguments, Object ret) {
        Result result = (Result) ret;
        // 记录dubbo的exception
        if (result != null && result.hasException()) {
            EntryFacade.INSTANCE.tryTraceEntry(result.getException(), RpcContext.getContext().isProviderSide(),
                    EntryFacade.DubboType.ALIBABA);
        }
        EntryFacade.INSTANCE.exit(EntryFacade.DubboType.ALIBABA);
    }

    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param throwable 增强时可能出现的异常
     */
    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        if (throwable != null) {
            EntryFacade.INSTANCE
                    .tryTraceEntry(throwable, RpcContext.getContext().isProviderSide(), EntryFacade.DubboType.ALIBABA);
        }
        EntryFacade.INSTANCE.exit(EntryFacade.DubboType.ALIBABA);
    }
}
