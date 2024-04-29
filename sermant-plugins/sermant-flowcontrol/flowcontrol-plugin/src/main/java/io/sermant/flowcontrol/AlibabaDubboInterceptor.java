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

package io.sermant.flowcontrol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.LogUtils;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.context.FlowControlContext;
import io.sermant.flowcontrol.common.entity.DubboRequestEntity;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.common.util.ConvertUtils;
import io.sermant.flowcontrol.common.util.DubboAttachmentsHelper;
import io.sermant.flowcontrol.service.InterceptorSupporter;

import java.util.Locale;

/**
 * Enhanced class after intercepting Alibaba Dubbo to define sentinel resources
 *
 * @author zhouss
 * @since 2022-02-10
 */
public class AlibabaDubboInterceptor extends InterceptorSupporter {
    private final String className = AlibabaDubboInterceptor.class.getName();

    /**
     * Convert alibaba dubbo. Note that this method is not extractable，Because host dependencies can only be loaded by
     * this interceptor, pulling out results in classes not being found.
     *
     * @param invoker invoker
     * @param invocation invoker information
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
            // For generalized interfaces, you can obtain the actual interface and version name from the url,
            // The method name is obtained based on parameters and is the first parameter of the requested method
            interfaceName = url.getParameter(CommonConst.GENERIC_INTERFACE_KEY, interfaceName);
            final Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 0 && arguments[0] instanceof String) {
                methodName = (String) invocation.getArguments()[0];
            }
            isGeneric = true;
        }

        // High version using API invocation.getTargetServiceUniqueName access path，
        // versions and interfaces are used here to achieve the same end result
        String apiPath = ConvertUtils.buildApiPath(interfaceName, version, methodName);
        final boolean isProvider = isProvider(curInvoker);
        return new DubboRequestEntity(apiPath, DubboAttachmentsHelper.resolveAttachments(invocation, false),
                isProvider ? RequestType.SERVER : RequestType.CLIENT, getApplication(url, interfaceName, isProvider),
                isGeneric);
    }

    private String getApplication(URL url, String interfaceName, boolean isProvider) {
        if (isProvider) {
            return url.getParameter(CommonConst.DUBBO_APPLICATION);
        }

        // Get it from the cache first, otherwise get remote.application from the url
        return DubboApplicationCache.INSTANCE.getApplicationCache().getOrDefault(interfaceName,
                url.getParameter(CommonConst.DUBBO_REMOTE_APPLICATION));
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) {
        LogUtils.printDubboRequestBeforePoint(context);
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
        LogUtils.printDubboRequestAfterPoint(context);
        return context;
    }

    @Override
    protected final ExecuteContext doThrow(ExecuteContext context) {
        chooseDubboService().onThrow(className, context.getThrowable(), isProvider(context));
        LogUtils.printDubboRequestOnThrowPoint(context);
        return context;
    }

    @Override
    protected boolean canInvoke(ExecuteContext context) {
        return true;
    }
}
