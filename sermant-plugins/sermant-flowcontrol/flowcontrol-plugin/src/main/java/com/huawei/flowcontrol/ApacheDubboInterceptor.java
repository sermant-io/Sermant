/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
import com.huawei.flowcontrol.common.util.DubboAttachmentsHelper;
import com.huawei.flowcontrol.service.InterceptorSupporter;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.LogUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Enhanced class after intercepting Apache Dubbo to define sentinel resources
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class ApacheDubboInterceptor extends InterceptorSupporter {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String LOW_VERSION_RPC_RESULT = "org.apache.dubbo.rpc.RpcResult";

    private final String className = ApacheDubboInterceptor.class.getName();

    /**
     * Convert apache dubbo. Note that this method is not extractable，Because host dependencies can only be loaded by
     * this interceptor, pulling out results in classes not being found.
     *
     * @param invoker invoker
     * @param invocation invoker information
     * @return DubboRequestEntity
     */
    private DubboRequestEntity convertToApacheDubboEntity(Invocation invocation, Invoker<?> invoker) {
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
        return new DubboRequestEntity(apiPath, DubboAttachmentsHelper.resolveAttachments(invocation, true),
                isProvider ? RequestType.SERVER : RequestType.CLIENT,
                getApplication(url, interfaceName, isProvider), isGeneric);
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
        if (allArguments[1] instanceof Invocation) {
            final FlowControlResult result = new FlowControlResult();
            Invocation invocation = (Invocation) allArguments[1];
            chooseDubboService().onBefore(className, convertToApacheDubboEntity(invocation,
                    (Invoker<?>) allArguments[0]), result, isProvider(context));
            if (!result.isSkip()) {
                return context;
            }
            skipResult(context, invocation, (Invoker<?>) allArguments[0], result);
        }
        return context;
    }

    private void skipResult(ExecuteContext context, Invocation invocation, Invoker<?> invoker,
            FlowControlResult result) {
        if (isLowApacheDubbo()) {
            skipWithLowVersion(context, invocation, invoker, result);
        } else {
            skipWithHighVersion(context, invocation, invoker, result);
        }
        FlowControlContext.INSTANCE.triggerFlowControl();
    }

    private void skipWithLowVersion(ExecuteContext context, Invocation invocation, Invoker<?> invoker,
            FlowControlResult result) {
        Optional<Object> rpcResult;
        if (result.getResponse().isReplaceResult()) {
            rpcResult = ReflectUtils.buildWithConstructor(LOW_VERSION_RPC_RESULT,
                    new Class[]{Object.class},
                    new Object[]{result.getResponse().getResult()});
        } else {
            rpcResult = ReflectUtils.buildWithConstructor(LOW_VERSION_RPC_RESULT,
                    new Class[]{Throwable.class},
                    new Object[]{wrapException(invocation, invoker, result)});
        }
        if (rpcResult.isPresent()) {
            context.skip(rpcResult.get());
        } else {
            LOGGER.warning("Can not find class RpcResult at dubbo version below 2.7.3(not include)");
        }
    }

    private void skipWithHighVersion(ExecuteContext context, Invocation invocation, Invoker<?> invoker,
            FlowControlResult result) {
        if (result.getResponse().isReplaceResult()) {
            context.skip(AsyncRpcResult.newDefaultAsyncResult(result.getResponse().getResult(), invocation));
        } else {
            context.skip(AsyncRpcResult.newDefaultAsyncResult(wrapException(invocation, invoker, result), invocation));
        }
    }

    /**
     * Determine whether the dubbo version is 2.7.0-2.7.3 (not included): The demarcation point is determined by the
     * AsyncRpcResult constructor. As of 2.7.3, AsyncRpcResult has changed its implementation to inherit CompleteFuture
     *
     * @return whether it is in the marked interval
     */
    private boolean isLowApacheDubbo() {
        return ReflectUtils.findConstructor(AsyncRpcResult.class, new Class[]{CompletableFuture.class}).isPresent();
    }

    private RpcException wrapException(Invocation invocation, Invoker<?> invoker, FlowControlResult result) {
        final DubboRequestEntity entity = convertToApacheDubboEntity(invocation, invoker);
        return new RpcException(result.getResponse().getCode(),
                String.format(Locale.ENGLISH, "Failed to invoke%s service %s: %s",
                        entity.isGeneric() ? " generic" : "", entity.getApiPath(), result.buildResponseMsg()));
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
