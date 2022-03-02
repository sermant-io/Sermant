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
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.util.ConvertUtils;
import com.huawei.flowcontrol.service.InterceptorSupporter;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import java.util.HashMap;
import java.util.Locale;

/**
 * apache dubbo拦截后的增强类,埋点定义sentinel资源
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class ApacheDubboInterceptor extends InterceptorSupporter {
    /**
     * 转换apache dubbo 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param invocation 调用信息
     * @return DubboRequestEntity
     */
    private DubboRequestEntity convertToApacheDubboEntity(org.apache.dubbo.rpc.Invocation invocation) {
        // 高版本使用api invocation.getTargetServiceUniqueName获取路径，此处使用版本加接口，达到的最终结果一致
        String apiPath = ConvertUtils.buildApiPath(invocation.getInvoker().getInterface().getName(),
            invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION), invocation.getMethodName());
        return new DubboRequestEntity(apiPath, new HashMap<>(invocation.getAttachments()));
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) {
        final Object[] allArguments = context.getArguments();
        if (allArguments[1] instanceof Invocation) {
            final FlowControlResult result = new FlowControlResult();
            Invocation invocation = (Invocation) allArguments[1];
            chooseDubboService().onBefore(convertToApacheDubboEntity(invocation), result,
                RpcContext.getContext().isProviderSide());
            if (result.isSkip()) {
                context.skip(AsyncRpcResult.newDefaultAsyncResult(
                    wrapException(invocation, (Invoker<?>) allArguments[0], result.getResult().getMsg()), invocation));
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
    protected final ExecuteContext doAfter(ExecuteContext context) {
        Result result = (Result) context.getResult();
        if (result != null) {
            chooseDubboService().onAfter(result, RpcContext.getContext().isProviderSide(), result.hasException());
        }
        return context;
    }

    @Override
    protected final ExecuteContext doThrow(ExecuteContext context) {
        chooseDubboService().onThrow(context.getThrowable(), RpcContext.getContext().isProviderSide());
        return context;
    }
}
