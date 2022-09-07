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
import com.huawei.flowcontrol.common.context.FlowControlContext;
import com.huawei.flowcontrol.common.entity.DubboRequestEntity;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity.RequestType;
import com.huawei.flowcontrol.common.util.ConvertUtils;
import com.huawei.flowcontrol.service.InterceptorSupporter;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;

import java.util.Collections;
import java.util.Locale;

/**
 * alibaba dubbo拦截后的增强类 埋点定义sentinel资源
 *
 * @author zhouss
 * @since 2022-02-10
 */
public class AlibabaDubboInterceptor extends InterceptorSupporter {
    private final String className = AlibabaDubboInterceptor.class.getName();

    /**
     * 转换apache dubbo 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param invoker 调用器
     * @param invocation 调用信息
     * @return DubboRequestEntity
     */
    private DubboRequestEntity convertToAlibabaDubboEntity(Invocation invocation, Invoker<?> invoker) {
        Invoker<?> curInvoker = invocation.getInvoker();
        if (curInvoker == null) {
            curInvoker = invoker;
        }
        String interfaceName = curInvoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        String version = invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION);
        final URL url = curInvoker.getUrl();
        boolean isGeneric = false;
        if (version == null) {
            version = url.getParameter(CommonConst.URL_VERSION_KEY, ConvertUtils.ABSENT_VERSION);
        }
        if (ConvertUtils.isGenericService(interfaceName, methodName)) {
            // 针对泛化接口, 实际接口、版本名通过url获取, 方法名基于参数获取, 为请求方法的第一个参数
            interfaceName = url.getParameter(CommonConst.GENERIC_INTERFACE_KEY, interfaceName);
            final Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 0 && arguments[0] instanceof String) {
                methodName = (String) invocation.getArguments()[0];
            }
            isGeneric = true;
        }

        // 高版本使用api invocation.getTargetServiceUniqueName获取路径，此处使用版本加接口，达到的最终结果一致
        String apiPath = ConvertUtils.buildApiPath(interfaceName, version, methodName);
        final boolean isProvider = isProvider(curInvoker);
        return new DubboRequestEntity(apiPath, Collections.unmodifiableMap(invocation.getAttachments()),
                isProvider ? RequestType.SERVER : RequestType.CLIENT, getApplication(url, interfaceName, isProvider),
                isGeneric);
    }

    private String getApplication(URL url, String interfaceName, boolean isProvider) {
        if (isProvider) {
            return url.getParameter(CommonConst.DUBBO_APPLICATION);
        }

        // 首先从缓存拿, 否则从url取remote.application
        return DubboApplicationCache.INSTANCE.getApplicationCache().getOrDefault(interfaceName,
                url.getParameter(CommonConst.DUBBO_REMOTE_APPLICATION));
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) {
        final Object[] allArguments = context.getArguments();
        final FlowControlResult result = new FlowControlResult();
        if (allArguments[1] instanceof Invocation) {
            Invocation invocation = (Invocation) allArguments[1];
            if (invocation.getInvoker() != null) {
                chooseDubboService().onBefore(className, convertToAlibabaDubboEntity(invocation,
                        (Invoker<?>) allArguments[0]), result, isProvider(context));
                if (!result.isSkip()) {
                    return context;
                }
                skipResult(context, invocation, (Invoker<?>) allArguments[0], result);
            } else {
                LoggerFactory.getLogger().warning("Not found down stream invoker, it will skip flow control check!");
            }
        }
        return context;
    }

    private void skipResult(ExecuteContext context, Invocation invocation, Invoker<?> invoker,
            FlowControlResult result) {
        if (result.getResponse().isReplaceResult()) {
            context.skip(new RpcResult(result.getResponse().getResult()));
        } else {
            context.skip(new RpcResult(wrapException(invocation, invoker, result)));
        }
        FlowControlContext.INSTANCE.triggerFlowControl();
    }

    private boolean isProvider(ExecuteContext context) {
        final Object argument = context.getArguments()[0];
        if (argument instanceof Invoker) {
            Invoker<?> invoker = (Invoker<?>) argument;
            return isProvider(invoker);
        }
        return false;
    }

    private boolean isProvider(Invoker<?> invoker) {
        return !CommonConst.DUBBO_CONSUMER.equals(invoker.getUrl().getParameter(CommonConst.DUBBO_SIDE,
                CommonConst.DUBBO_PROVIDER));
    }

    private RpcException wrapException(Invocation invocation, Invoker<?> invoker, FlowControlResult result) {
        final DubboRequestEntity entity = convertToAlibabaDubboEntity(invocation, invoker);
        return new RpcException(result.getResponse().getCode(),
                String.format(Locale.ENGLISH, "Failed to invoke%s service %s: %s",
                        entity.isGeneric() ? " generic" : "", entity.getApiPath(), result.buildResponseMsg()));
    }

    @Override
    protected final ExecuteContext doAfter(ExecuteContext context) {
        Result result = (Result) context.getResult();
        final boolean isProvider = isProvider(context);
        if (result != null) {
            chooseDubboService().onAfter(className, result, isProvider, result.hasException());
        }
        if (isProvider) {
            FlowControlContext.INSTANCE.clear();
        }
        return context;
    }

    @Override
    protected final ExecuteContext doThrow(ExecuteContext context) {
        chooseDubboService().onThrow(className, context.getThrowable(), isProvider(context));
        return context;
    }

    @Override
    protected boolean canInvoke(ExecuteContext context) {
        return true;
    }
}
