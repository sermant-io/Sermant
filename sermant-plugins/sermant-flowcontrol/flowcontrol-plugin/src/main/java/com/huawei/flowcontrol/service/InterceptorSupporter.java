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

package com.huawei.flowcontrol.service;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.enums.FlowFramework;
import com.huawei.flowcontrol.common.handler.retry.RetryContext;
import com.huawei.flowcontrol.common.support.ReflectMethodCacheSupport;
import com.huawei.flowcontrol.retry.handler.RetryHandlerV2;
import com.huawei.flowcontrol.service.rest4j.DubboRest4jService;
import com.huawei.flowcontrol.service.rest4j.HttpRest4jService;
import com.huawei.flowcontrol.service.sen.DubboSenService;
import com.huawei.flowcontrol.service.sen.HttpSenService;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.service.ServiceManager;

import io.github.resilience4j.retry.RetryConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 拦截器功能支持
 *
 * @author zhouss
 * @since 2022-01-25
 */
public abstract class InterceptorSupporter extends ReflectMethodCacheSupport implements Interceptor {
    /**
     * 标记当前请求重试中
     */
    protected static final String RETRY_KEY = "$$$$RETRY$$$";

    protected static final String RETRY_VALUE = "$$$$RETRY_VALUE$$$";

    protected final RetryHandlerV2 retryHandler = new RetryHandlerV2();

    private final ReentrantLock lock = new ReentrantLock();

    private DubboService dubboService;

    private HttpService httpService;

    /**
     * 获取选择后的DUBBO服务
     *
     * @return DubboService
     */
    protected final DubboService chooseDubboService() {
        if (dubboService == null) {
            lock.lock();
            try {
                final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
                if (pluginConfig.getFlowFramework() == FlowFramework.SENTINEL) {
                    dubboService = ServiceManager.getService(DubboSenService.class);
                } else {
                    dubboService = ServiceManager.getService(DubboRest4jService.class);
                }
            } finally {
                lock.unlock();
            }
        }
        return dubboService;
    }

    /**
     * 获取选择后的HTTP服务
     *
     * @return HttpService
     */
    protected final HttpService chooseHttpService() {
        if (httpService == null) {
            lock.lock();
            try {
                final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
                if (pluginConfig.getFlowFramework() == FlowFramework.SENTINEL) {
                    httpService = ServiceManager.getService(HttpSenService.class);
                } else {
                    httpService = ServiceManager.getService(HttpRest4jService.class);
                }
            } finally {
                lock.unlock();
            }
        }
        return httpService;
    }

    protected final Supplier<Object> createRetryFunc(Object obj, Method method, Object[] allArguments, Object result) {
        return () -> {
            method.setAccessible(true);
            try {
                return method.invoke(obj, allArguments);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                // ignored
            }
            return result;
        };
    }

    /**
     * 进行重试前的判断，若不满足条件直接返回， 防止多调用一次宿主应用接口
     *
     * @param retry     重试执行器
     * @param result    结果
     * @param throwable 第一次执行异常信息
     * @return 是否核对通过
     */
    protected final boolean needRetry(io.github.resilience4j.retry.Retry retry, Object result, Throwable throwable) {
        final long interval = retry.getRetryConfig().getIntervalBiFunction().apply(1, null);
        final RetryConfig retryConfig = retry.getRetryConfig();
        boolean isNeedRetry = isMatchResult(result, retryConfig.getResultPredicate()) || isTargetException(throwable,
            retryConfig.getExceptionPredicate());
        if (isNeedRetry) {
            try {
                // 按照第一次等待时间等待
                Thread.sleep(interval);
            } catch (InterruptedException ignored) {
                // ignored
            }
        }
        return isNeedRetry;
    }

    private boolean isMatchResult(Object result, Predicate<Object> resultPredicate) {
        return result != null && resultPredicate.test(result);
    }

    private boolean isTargetException(Throwable throwable, Predicate<Throwable> exceptionPredicate) {
        return throwable != null && exceptionPredicate.test(throwable);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        if (RetryContext.INSTANCE.isMarkedRetry()) {
            return context;
        }
        return doBefore(context);
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (RetryContext.INSTANCE.isMarkedRetry()) {
            return context;
        }
        return doAfter(context);
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        if (RetryContext.INSTANCE.isMarkedRetry()) {
            return context;
        }
        return doThrow(context);
    }

    /**
     * 前置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    protected abstract ExecuteContext doBefore(ExecuteContext context) throws Exception;

    /**
     * 后置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    protected abstract ExecuteContext doAfter(ExecuteContext context) throws Exception;

    /**
     * 异常触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     */
    protected ExecuteContext doThrow(ExecuteContext context) {
        return context;
    }
}
