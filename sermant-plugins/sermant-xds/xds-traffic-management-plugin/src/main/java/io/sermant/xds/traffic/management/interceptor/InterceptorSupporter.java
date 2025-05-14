/*
 * Copyright (C) 2022-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.xds.traffic.management.interceptor;

import io.github.resilience4j.retry.RetryConfig;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.xds.common.config.XdsTrafficManagementConfig;
import io.sermant.xds.common.exception.InvokerWrapperException;
import io.sermant.xds.common.flowcontrol.retry.RetryContext;
import io.sermant.xds.traffic.management.handler.RetryHandler;
import io.sermant.xds.traffic.management.service.XdsHttpFlowControlService;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * interceptor function supported
 *
 * @author zhouss
 * @since 2022-01-25
 */
public abstract class InterceptorSupporter implements Interceptor {
    protected static final Logger LOGGER = LoggerFactory.getLogger();

    protected final XdsTrafficManagementConfig xdsTrafficManagementConfig;

    private final XdsHttpFlowControlService xdsHttpFlowControlService;

    private final RetryHandler retryHandler;

    /**
     * constructor
     */
    protected InterceptorSupporter() {
        xdsTrafficManagementConfig = PluginConfigManager.getPluginConfig(XdsTrafficManagementConfig.class);
        xdsHttpFlowControlService =
                PluginServiceManager.getPluginService(XdsHttpFlowControlService.class);
        retryHandler = new RetryHandler();
    }

    /**
     * get retry handler
     *
     * @return RetryHandler
     */
    protected final RetryHandler getRetryHandler() {
        return retryHandler;
    }

    /**
     * gets the selected XDS flow control service
     *
     * @return HttpService
     */
    protected XdsHttpFlowControlService getXdsHttpFlowControlService() {
        return xdsHttpFlowControlService;
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
    protected boolean isNeedRetry(io.github.resilience4j.retry.Retry retry, Object result, Throwable throwable) {
        final RetryConfig retryConfig = retry.getRetryConfig();
        boolean isNeedRetry = isMatchResult(result, retryConfig.getResultPredicate()) || isTargetException(throwable,
                retryConfig.getExceptionPredicate());
        if (isNeedRetry) {
            final long interval = retryConfig.getIntervalBiFunction().apply(1, null);
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
    public ExecuteContext onThrow(ExecuteContext context) {
        if (canInvoke(context)) {
            return doThrow(context);
        }
        return context;
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
