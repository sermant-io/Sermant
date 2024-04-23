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

package io.sermant.flowcontrol.service;

import io.github.resilience4j.retry.RetryConfig;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.enums.FlowFramework;
import io.sermant.flowcontrol.common.exception.InvokerWrapperException;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.common.support.ReflectMethodCacheSupport;
import io.sermant.flowcontrol.retry.handler.RetryHandlerV2;
import io.sermant.flowcontrol.service.rest4j.DubboRest4jService;
import io.sermant.flowcontrol.service.rest4j.HttpRest4jService;
import io.sermant.flowcontrol.service.sen.DubboSenService;
import io.sermant.flowcontrol.service.sen.HttpSenService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * interceptor function supported
 *
 * @author zhouss
 * @since 2022-01-25
 */
public abstract class InterceptorSupporter extends ReflectMethodCacheSupport implements Interceptor {
    /**
     * flag that the current request is in retry
     */
    protected static final String RETRY_KEY = "$$$$RETRY$$$";

    /**
     * flag that the current request is in retry
     */
    protected static final String RETRY_VALUE = "$$$$RETRY_VALUE$$$";

    /**
     * apache dubbo Cluster class name
     */
    protected static final String APACHE_DUBBO_CLUSTER_CLASS_NAME = "org.apache.dubbo.rpc.cluster.Cluster";

    /**
     * alibaba dubbo Cluster class name
     */
    protected static final String ALIBABA_DUBBO_CLUSTER_CLASS_NAME = "com.alibaba.dubbo.rpc.cluster.Cluster";

    private static final String REFUSE_REPLACE_INVOKER = "close";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * flow control configuration
     */
    protected final FlowControlConfig flowControlConfig;

    private final ReentrantLock lock = new ReentrantLock();

    private RetryHandlerV2 retryHandler = null;

    private DubboService dubboService;

    private HttpService httpService;

    /**
     * constructor
     */
    protected InterceptorSupporter() {
        flowControlConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
    }

    /**
     * get retry handler
     *
     * @return RetryHandlerV2
     */
    protected final RetryHandlerV2 getRetryHandler() {
        if (retryHandler == null) {
            lock.lock();
            try {
                retryHandler = new RetryHandlerV2();
            } finally {
                lock.unlock();
            }
        }
        return retryHandler;
    }

    /**
     * get the selected dubbo service
     *
     * @return DubboService
     */
    protected final DubboService chooseDubboService() {
        if (dubboService == null) {
            lock.lock();
            try {
                if (flowControlConfig.getFlowFramework() == FlowFramework.SENTINEL) {
                    dubboService = PluginServiceManager.getPluginService(DubboSenService.class);
                } else {
                    dubboService = PluginServiceManager.getPluginService(DubboRest4jService.class);
                }
            } finally {
                lock.unlock();
            }
        }
        return dubboService;
    }

    /**
     * gets the selected http service
     *
     * @return HttpService
     */
    protected final HttpService chooseHttpService() {
        if (httpService == null) {
            lock.lock();
            try {
                if (flowControlConfig.getFlowFramework() == FlowFramework.SENTINEL) {
                    httpService = PluginServiceManager.getPluginService(HttpSenService.class);
                } else {
                    httpService = PluginServiceManager.getPluginService(HttpRest4jService.class);
                }
            } finally {
                lock.unlock();
            }
        }
        return httpService;
    }

    /**
     * create retry method
     *
     * @param obj enhancement class
     * @param method target method
     * @param allArguments method parameter
     * @param result default result
     * @return Method
     * @throws InvokerWrapperException InvokerWrapperException
     */
    protected final Supplier<Object> createRetryFunc(Object obj, Method method, Object[] allArguments, Object result) {
        return () -> {
            method.setAccessible(true);
            try {
                return method.invoke(obj, allArguments);
            } catch (IllegalAccessException ignored) {
                // ignored
            } catch (InvocationTargetException ex) {
                throw new InvokerWrapperException(ex.getTargetException());
            }
            return result;
        };
    }

    /**
     * Judgment before retrying: If the conditions are not met, the host application interface is returned directly to
     * prevent multiple calls
     *
     * @param retry Retry executor
     * @param result result
     * @param throwable Exception information for the first execution
     * @return check through
     */
    protected final boolean needRetry(io.github.resilience4j.retry.Retry retry, Object result, Throwable throwable) {
        final long interval = retry.getRetryConfig().getIntervalBiFunction().apply(1, null);
        final RetryConfig retryConfig = retry.getRetryConfig();
        boolean isNeedRetry = isMatchResult(result, retryConfig.getResultPredicate()) || isTargetException(throwable,
                retryConfig.getExceptionPredicate());
        if (isNeedRetry) {
            try {
                // wait according to the first wait time
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Interruption error:", e);
            }
        }
        return isNeedRetry;
    }

    /**
     * print error log
     *
     * @param throwable throwable
     */
    protected void log(Throwable throwable) {
        LOGGER.log(Level.INFO, "Failed to invoke target", getExMsg(throwable));
        LOGGER.log(Level.FINE, "Failed to invoke target", (throwable instanceof InvokerWrapperException)
                ? ((InvokerWrapperException) throwable).getRealException() : throwable);
    }

    private boolean isMatchResult(Object result, Predicate<Object> resultPredicate) {
        return result != null && resultPredicate.test(result);
    }

    private boolean isTargetException(Throwable throwable, Predicate<Throwable> exceptionPredicate) {
        return throwable != null && exceptionPredicate.test(throwable);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        if (canInvoke(context)) {
            return doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (canInvoke(context)) {
            return doAfter(context);
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        if (canInvoke(context)) {
            return doThrow(context);
        }
        return context;
    }

    /**
     * whether the dubbo cluster loader can be injected
     *
     * @param className loader type
     * @return injectable or not
     */
    protected final boolean canInjectClusterInvoker(String className) {
        boolean isClusterLoader =
                APACHE_DUBBO_CLUSTER_CLASS_NAME.equals(className) || ALIBABA_DUBBO_CLUSTER_CLASS_NAME.equals(className);
        return isClusterLoader && !REFUSE_REPLACE_INVOKER.equals(flowControlConfig.getRetryClusterInvoker());
    }

    /**
     * parse exception message
     *
     * @param throwable throwable
     * @return msg
     */
    protected String getExMsg(Throwable throwable) {
        return getRealCause(throwable).toString();
    }

    /**
     * get true exception
     *
     * @param throwable exception message
     * @return true exception
     */
    protected Throwable getRealCause(Throwable throwable) {
        if (throwable instanceof InvokerWrapperException) {
            final Throwable realException = ((InvokerWrapperException) throwable).getRealException();
            return realException.getCause() != null ? realException.getCause() : realException;
        }
        return throwable;
    }

    /**
     * pre-trigger point
     *
     * @param context execution context
     * @return execution context
     * @throws Exception execute exception
     */
    protected abstract ExecuteContext doBefore(ExecuteContext context) throws Exception;

    /**
     * post-trigger point
     *
     * @param context execution context
     * @return execution context
     * @throws Exception execute exception
     */
    protected abstract ExecuteContext doAfter(ExecuteContext context) throws Exception;

    /**
     * exception trigger point
     *
     * @param context execution context
     * @return execution context
     */
    protected ExecuteContext doThrow(ExecuteContext context) {
        return context;
    }

    /**
     * whether internal method logic can be invoked
     *
     * @param context context
     * @return whether internal method logic can be invoked
     */
    protected boolean canInvoke(ExecuteContext context) {
        return !RetryContext.INSTANCE.isMarkedRetry();
    }
}
