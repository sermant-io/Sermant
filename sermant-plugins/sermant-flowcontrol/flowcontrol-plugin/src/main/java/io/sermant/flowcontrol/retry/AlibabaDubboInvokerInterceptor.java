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

package io.sermant.flowcontrol.retry;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import com.alibaba.dubbo.rpc.service.GenericException;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.entity.DubboRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.common.exception.InvokerWrapperException;
import io.sermant.flowcontrol.common.handler.retry.AbstractRetry;
import io.sermant.flowcontrol.common.handler.retry.Retry;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.common.util.ConvertUtils;
import io.sermant.flowcontrol.service.InterceptorSupporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * An enhanced class after the alibaba dubbo intercept, buried to define sentinel resources
 *
 * @author zhouss
 * @since 2022-02-10
 */
public class AlibabaDubboInvokerInterceptor extends InterceptorSupporter {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int LOADER_BALANCE_INDEX = 2;

    private final Retry retry = new AlibabaDubboRetry();

    /**
     * Convert alibaba dubbo. Note that this method is not extractable，Because host dependencies can only be loaded by
     * this interceptor, pulling out results in classes not being found.
     *
     * @param invocation invoke information
     * @return DubboRequestEntity
     */
    private DubboRequestEntity convertToAlibabaDubboEntity(Invocation invocation) {
        final Invoker<?> invoker = invocation.getInvoker();
        String interfaceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        String version = invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION);
        if (ConvertUtils.isGenericService(interfaceName, methodName)) {
            // For generalized interfaces, you can obtain the actual interface and version name from the url,
            // The method name is obtained based on parameters and is the first parameter of the requested method
            final URL url = invoker.getUrl();
            interfaceName = url.getParameter(CommonConst.GENERIC_INTERFACE_KEY, interfaceName);
            final Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 0 && arguments[0] instanceof String) {
                methodName = (String) invocation.getArguments()[0];
            }
            version = url.getParameter(CommonConst.URL_VERSION_KEY, version);
        }

        // High version using API invocation.getTargetServiceUniqueName access path，
        // versions and interfaces are used here to achieve the same end result
        String apiPath = ConvertUtils.buildApiPath(interfaceName, version, methodName);
        return new DubboRequestEntity(apiPath, Collections.unmodifiableMap(invocation.getAttachments()),
                RequestType.CLIENT, invoker.getUrl().getParameter(CommonConst.DUBBO_REMOTE_APPLICATION));
    }

    private Object invokeRetryMethod(Object obj, Object[] allArguments, Object ret, boolean isNeedThrow,
            boolean isRetry) {
        try {
            if (obj instanceof AbstractClusterInvoker) {
                final Invocation invocation = (Invocation) allArguments[0];
                final List<Invoker<?>> invokers = (List<Invoker<?>>) allArguments[1];
                final Optional<Method> checkInvokersOption = getMethodCheckInvokers();
                final Optional<Method> selectOption = getMethodSelect();

                if (!checkInvokersOption.isPresent() || !selectOption.isPresent()) {
                    LOGGER.warning(String.format(Locale.ENGLISH, "It does not support retry for class %s",
                            obj.getClass().getCanonicalName()));
                    return ret;
                }
                final Method checkInvokers = checkInvokersOption.get();
                final Method select = selectOption.get();
                if (isRetry) {
                    invocation.getAttachments().put(RETRY_KEY, RETRY_VALUE);
                }

                // verify invokers
                checkInvokers.invoke(obj, invokers, invocation);
                LoadBalance loadBalance = (LoadBalance) allArguments[LOADER_BALANCE_INDEX];

                // select invoker
                final Invoker<?> invoke = (Invoker<?>) select.invoke(obj, loadBalance, invocation, invokers, null);

                // execute invoke
                final Result result = invoke.invoke(invocation);
                handleException(isNeedThrow, result);
                return result;
            }
        } catch (IllegalAccessException ex) {
            LOGGER.warning("No such Method ! " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            // For this exception, you need to get the target exception (true method exception)
            throw new InvokerWrapperException(ex.getTargetException());
        }
        return ret;
    }

    private void handleException(boolean isNeedThrow, Result result) {
        if (result.hasException() && isNeedThrow) {
            final Throwable exception = result.getException();
            if (exception instanceof GenericException) {
                throw (GenericException) exception;
            }
            throw new InvokerWrapperException(result.getException());
        }
    }

    private Optional<Method> getMethodSelect() {
        return getInvokerMethod("select", func -> {
            try {
                final Method method = AbstractClusterInvoker.class
                        .getDeclaredMethod("select", LoadBalance.class, Invocation.class, List.class, List.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ex) {
                LOGGER.warning("No such Method! " + ex.getMessage());
            }
            return placeHolderMethod;
        });
    }

    private Optional<Method> getMethodCheckInvokers() {
        return getInvokerMethod("checkInvokers", func -> {
            try {
                final Method method = AbstractClusterInvoker.class
                        .getDeclaredMethod("checkInvokers", List.class, Invocation.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ex) {
                LOGGER.warning("No such Method! " + ex.getMessage());
            }
            return placeHolderMethod;
        });
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) {
        context.skip(null);
        return context;
    }

    @Override
    protected final ExecuteContext doAfter(ExecuteContext context) {
        final Object[] allArguments = context.getArguments();
        final Invocation invocation = (Invocation) allArguments[0];
        Object result = context.getResult();
        try {
            // marks the current thread to perform retry
            RetryContext.INSTANCE.markRetry(retry);
            result = invokeRetryMethod(context.getObject(), allArguments, result, false, false);
            if (invocation.getInvoker() != null) {
                final List<io.github.resilience4j.retry.Retry> handlers = getRetryHandler()
                        .getHandlers(convertToAlibabaDubboEntity(invocation));
                if (!handlers.isEmpty() && needRetry(handlers.get(0), result, ((RpcResult) result).getException())) {
                    result = handlers.get(0).executeCheckedSupplier(
                            () -> invokeRetryMethod(context.getObject(), allArguments, context.getResult(), true,
                                    true));
                }
            } else {
                LOGGER.warning("Not found down stream invoker, it will skip retry check!");
            }
        } catch (Throwable throwable) {
            result = buildErrorResponse(throwable, invocation);
        } finally {
            RetryContext.INSTANCE.remove();
        }
        context.changeResult(result);
        return context;
    }

    private Object buildErrorResponse(Throwable throwable, Invocation invocation) {
        Throwable realException = throwable;
        if (throwable instanceof InvokerWrapperException) {
            realException = ((InvokerWrapperException) throwable).getRealException();
        }
        LOGGER.warning(String.format(Locale.ENGLISH, "Invoking method [%s] failed, reason : %s",
                invocation.getMethodName(), realException.getMessage()));
        return new RpcResult(realException);
    }

    @Override
    protected boolean canInvoke(ExecuteContext context) {
        return true;
    }

    /**
     * alibaba retry
     *
     * @since 2022-02-22
     */
    public static class AlibabaDubboRetry extends AbstractRetry {
        @Override
        public boolean isNeedRetry(Set<String> statusList, Object result) {
            // dubbo does not support status codes
            return false;
        }

        @Override
        public Class<? extends Throwable>[] retryExceptions() {
            return getRetryExceptions();
        }

        @Override
        public RetryFramework retryType() {
            return RetryFramework.ALIBABA_DUBBO;
        }
    }
}
