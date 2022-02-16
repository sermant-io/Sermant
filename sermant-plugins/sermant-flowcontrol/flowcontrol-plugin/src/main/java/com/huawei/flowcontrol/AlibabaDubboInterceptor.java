/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.entity.DubboRequestEntity;
import com.huawei.flowcontrol.common.entity.FixedResult;
import com.huawei.flowcontrol.common.enums.FlowControlEnum;
import com.huawei.flowcontrol.common.util.ConvertUtils;
import com.huawei.flowcontrol.service.InterceptorSupporter;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;

import org.apache.dubbo.rpc.Invoker;

import java.util.Locale;

/**
 * alibaba dubbo拦截后的增强类 埋点定义sentinel资源
 *
 * @author zhouss
 * @since 2022-02-10
 */
public class AlibabaDubboInterceptor extends InterceptorSupporter implements Interceptor {
    /**
     * 转换apache dubbo 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param invocation 调用信息
     * @return DubboRequestEntity
     */
    private static DubboRequestEntity convertToAlibabaDubboEntity(com.alibaba.dubbo.rpc.Invocation invocation) {
        // invocation.getTargetServiceUniqueName
        String apiPath = ConvertUtils.buildApiPath(invocation.getInvoker().getInterface().getName(),
            invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION), invocation.getMethodName());
        return new DubboRequestEntity(apiPath, invocation.getAttachments());
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        final Object[] allArguments = context.getArguments();
        final FixedResult result = new FixedResult();
        if (allArguments[1] instanceof Invocation) {
            Invocation invocation = (Invocation) allArguments[1];
            chooseDubboService().onBefore(convertToAlibabaDubboEntity(invocation), result,
                RpcContext.getContext().isProviderSide());
            if (result.isSkip() && result.getResult() instanceof FlowControlEnum) {
                context.skip(new RpcResult(wrapException(invocation, (Invoker<?>) allArguments[0],
                    ((FlowControlEnum) result.getResult()).getMsg())));
            }
        }
        return context;
    }

    private RpcException wrapException(Invocation invocation, Invoker<?> invoker, String msg) {
        return new RpcException(CommonConst.TOO_MANY_REQUEST_CODE,
            String.format(Locale.ENGLISH, "Failed to invoke service %s.%s: %s",
                invoker.getInterface().getName(), invocation.getMethodName(), msg));
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        Result result = (Result) context.getResult();
        if (result != null) {
            chooseDubboService().onAfter(result, RpcContext.getContext().isProviderSide(), result.hasException());
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        chooseDubboService().onThrow(context.getThrowable(), RpcContext.getContext().isProviderSide());
        return context;
    }
}
