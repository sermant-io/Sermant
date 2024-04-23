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

package io.sermant.flowcontrol.retry.cluster;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.utils.ClassUtils;
import io.sermant.flowcontrol.DubboApplicationCache;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.context.FlowControlContext;
import io.sermant.flowcontrol.common.entity.DubboRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.common.handler.retry.AbstractRetry;
import io.sermant.flowcontrol.common.handler.retry.Retry;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.common.util.ConvertUtils;
import io.sermant.flowcontrol.common.util.DubboAttachmentsHelper;
import io.sermant.flowcontrol.retry.handler.RetryHandlerV2;
import io.vavr.CheckedFunction0;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import org.apache.dubbo.rpc.service.GenericException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * apache dubbo invoker
 *
 * @param <T> return type
 * @author zhouss
 * @since 2022-03-04
 */
public class ApacheDubboClusterInvoker<T> extends AbstractClusterInvoker<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Retry retry = new ApacheDubboRetry();

    private final RetryHandlerV2 retryHandler = new RetryHandlerV2();

    private final Invoker<T> delegate;

    /**
     * apache dubbo cluster call
     *
     * @param directory service
     */
    public ApacheDubboClusterInvoker(Directory<T> directory) {
        this(directory, null);
    }

    /**
     * apache dubbo cluster call
     *
     * @param directory service
     * @param delegate original invoker, need to enable configuration{@link FlowControlConfig#isUseOriginInvoker()}
     */
    public ApacheDubboClusterInvoker(Directory<T> directory, Invoker<T> delegate) {
        super(directory);
        this.delegate = delegate;
    }

    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance)
            throws RpcException {
        RetryContext.INSTANCE.markRetry(retry);
        checkInvokers(invokers, invocation);
        final List<io.github.resilience4j.retry.Retry> handlers = retryHandler
                .getHandlers(convertToApacheDubboEntity(invocation, invokers.get(0)));
        final List<Invoker<T>> selected = new ArrayList<>();
        DecorateCheckedSupplier<Result> dcs = Decorators.ofCheckedSupplier(buildFunc(invocation, invokers,
                loadbalance, selected));
        io.github.resilience4j.retry.Retry retryRule = null;
        if (!handlers.isEmpty()) {
            // only one policy is supported for retry
            retryRule = handlers.get(0);
            dcs.withRetry(retryRule);
        }
        try {
            return dcs.get();
        } catch (RpcException ex) {
            log(retryRule, invocation);
            throw ex;
        } catch (Throwable ex) {
            log(retryRule, invocation);
            throw formatEx(ex);
        } finally {
            RetryContext.INSTANCE.remove();
            FlowControlContext.INSTANCE.clear();
            selected.clear();
        }
    }

    private RuntimeException formatEx(Throwable ex) {
        if (ex instanceof GenericException) {
            return (GenericException) ex;
        }

        // Note that this class may be phased out by the new version; dubbo3.1.0 is still in use
        final Optional<Class<?>> isExist = ClassUtils
                .loadClass("com.alibaba.dubbo.rpc.service.GenericException", Thread.currentThread()
                        .getContextClassLoader());
        if (isExist.isPresent() && ex instanceof com.alibaba.dubbo.rpc.service.GenericException) {
            return (com.alibaba.dubbo.rpc.service.GenericException) ex;
        }
        return new RpcException(ex.getMessage(), ex);
    }

    private void log(io.github.resilience4j.retry.Retry retryRule, Invocation invocation) {
        if (retryRule != null) {
            LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                    "Retry %d times failed for interface %s.%s", retryRule.getRetryConfig().getMaxAttempts() - 1,
                    invocation.getInvoker().getInterface().getName(), invocation.getMethodName()));
        }
    }

    private CheckedFunction0<Result> buildFunc(Invocation invocation, List<Invoker<T>> invokers,
            LoadBalance loadbalance, List<Invoker<T>> selected) {
        if (this.delegate == null) {
            return () -> {
                checkInvokers(invokers, invocation);
                Invoker<T> invoker = select(loadbalance, invocation, invokers, selected);
                selected.add(invoker);
                Result result = invoker.invoke(invocation);
                checkThrowEx(result);
                return result;
            };
        }
        return () -> {
            Result result = delegate.invoke(invocation);
            checkThrowEx(result);
            return result;
        };
    }

    private void checkThrowEx(Result result) throws Throwable {
        if (result != null && result.hasException() && !FlowControlContext.INSTANCE.isFlowControl()) {
            throw result.getException();
        }
    }

    /**
     * Convert apache dubbo. Note that this method is not extractable，Because host dependencies can only be loaded by
     * this interceptor, pulling out results in classes not being found.
     *
     * @param invocation invoker information
     * @param invoker invoker
     * @return DubboRequestEntity
     */
    private DubboRequestEntity convertToApacheDubboEntity(Invocation invocation, Invoker<T> invoker) {
        String interfaceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        String version = invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION);
        final URL url = invoker.getUrl();
        boolean isGeneric = false;
        if (version == null) {
            version = url.getParameter(CommonConst.URL_VERSION_KEY, ConvertUtils.ABSENT_VERSION);
        }
        if (ConvertUtils.isGenericService(interfaceName, methodName)) {
            // For generalized interfaces, you can obtain the actual interface and version name from the url,
            // The method name is obtained based on parameters and is the first parameter of the requested method
            isGeneric = true;
            interfaceName = url.getParameter(CommonConst.GENERIC_INTERFACE_KEY, interfaceName);
            final Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 0 && arguments[0] instanceof String) {
                methodName = (String) invocation.getArguments()[0];
            }
        }

        // High version using API invocation.getTargetServiceUniqueName access path，
        // versions and interfaces are used here to achieve the same end result
        String apiPath = ConvertUtils.buildApiPath(interfaceName, version, methodName);
        return new DubboRequestEntity(apiPath, DubboAttachmentsHelper.resolveAttachments(invocation, true),
                RequestType.CLIENT, getRemoteApplication(url, interfaceName), isGeneric);
    }

    private String getRemoteApplication(URL url, String interfaceName) {
        return DubboApplicationCache.INSTANCE.getApplicationCache()
                .getOrDefault(interfaceName, url.getParameter(CommonConst.DUBBO_REMOTE_APPLICATION));
    }

    /**
     * apache dubbo retry
     *
     * @since 2022-02-21
     */
    public static class ApacheDubboRetry extends AbstractRetry {
        @Override
        public boolean needRetry(Set<String> statusList, Object result) {
            // dubbo does not support status codes
            return false;
        }

        @Override
        public Class<? extends Throwable>[] retryExceptions() {
            return getRetryExceptions();
        }

        @Override
        public RetryFramework retryType() {
            return RetryFramework.APACHE_DUBBO;
        }
    }
}
