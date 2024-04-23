/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.flowcontrol.res4j.chain.handler;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.sermant.flowcontrol.res4j.adaptor.CircuitBreakerAdaptor;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;
import io.sermant.flowcontrol.res4j.exceptions.CircuitBreakerException;
import io.sermant.flowcontrol.res4j.handler.CircuitBreakerHandler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Circuit Breaker handler
 *
 * @author zhouss
 * @since 2022-07-05
 */
public class CircuitBreakerRequestHandler extends FlowControlHandler<CircuitBreaker> {
    private static final String CONTEXT_NAME = CircuitBreakerRequestHandler.class.getName();

    private static final String START_TIME = CONTEXT_NAME + "_START_TIME";

    private final CircuitBreakerHandler circuitBreakerHandler = getHandler();

    @Override
    public void onBefore(RequestContext context, Set<String> businessNames) {
        final List<CircuitBreaker> circuitBreakers = circuitBreakerHandler.createOrGetHandlers(businessNames);
        if (!circuitBreakers.isEmpty()) {
            for (CircuitBreaker circuitBreaker : circuitBreakers) {
                checkForceOpen(circuitBreaker);
                checkCircuitBreakerState(circuitBreaker);
            }

            // 这里使用内置方法获取时间, 列表中的每个熔断器时间均一致，因此取第一个
            context.save(getStartTime(), circuitBreakers.get(0).getCurrentTimestamp());
            context.save(getContextName(), circuitBreakers);
        }
        super.onBefore(context, businessNames);
    }

    private void checkCircuitBreakerState(CircuitBreaker circuitBreaker) {
        final boolean isSuccess = circuitBreaker.tryAcquirePermission();
        if (!isSuccess) {
            throw CircuitBreakerException.createException(circuitBreaker);
        }
    }

    /**
     * 强制开启状态直接抛出熔断异常
     *
     * @param circuitBreaker 熔断器
     */
    private void checkForceOpen(CircuitBreaker circuitBreaker) {
        if (circuitBreaker instanceof CircuitBreakerAdaptor) {
            if (((CircuitBreakerAdaptor) circuitBreaker).isForceOpen()) {
                // 强制开启则直接抛出异常
                throw CircuitBreakerException.createException(circuitBreaker);
            }
        }
    }

    @Override
    public void onThrow(RequestContext context, Set<String> businessNames, Throwable throwable) {
        process(context, throwable, null, false);
        super.onThrow(context, businessNames, throwable);
    }

    @Override
    public void onResult(RequestContext context, Set<String> businessNames, Object result) {
        try {
            process(context, null, result, true);
        } finally {
            context.remove(getContextName());
            context.remove(getStartTime());
        }
        super.onResult(context, businessNames, result);
    }

    private void process(RequestContext context, Throwable throwable, Object result, boolean isResult) {
        final Long startTime = context.get(getStartTime(), Long.class);
        final List<CircuitBreaker> circuitBreakers = getHandlersFromCache(context.getSourceName(), getContextName());
        if (startTime == null || circuitBreakers == null || circuitBreakers.isEmpty()) {
            return;
        }
        long duration = circuitBreakers.get(0).getCurrentTimestamp() - startTime;
        final TimeUnit timestampUnit = circuitBreakers.get(0).getTimestampUnit();
        if (throwable != null) {
            circuitBreakers.forEach(circuitBreaker -> circuitBreaker.onError(duration, timestampUnit, throwable));
        }
        if (isResult && context.get(HandlerConstants.OCCURRED_REQUEST_EXCEPTION, Throwable.class) == null) {
            circuitBreakers.forEach(circuitBreaker -> circuitBreaker.onResult(duration, timestampUnit, result));
        }
    }

    @Override
    protected boolean isSkip(RequestContext context, Set<String> businessNames) {
        return isForceClose(circuitBreakerHandler.createOrGetHandlers(businessNames));
    }

    private boolean isForceClose(List<CircuitBreaker> circuitBreakers) {
        for (CircuitBreaker circuitBreaker : circuitBreakers) {
            if (!(circuitBreaker instanceof CircuitBreakerAdaptor)) {
                continue;
            }
            if (((CircuitBreakerAdaptor) circuitBreaker).isForceClosed()) {
                // 强制关闭则跳过当前处理器逻辑
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前缓存上下文名称
     *
     * @return 缓存上下文名称
     */
    @Override
    protected String getContextName() {
        return CONTEXT_NAME;
    }

    /**
     * 获取当前缓存上下文开始时间
     *
     * @return 缓存上下文开始时间
     */
    protected String getStartTime() {
        return START_TIME;
    }

    @Override
    public int getOrder() {
        return HandlerConstants.CIRCUIT_BREAKER_ORDER;
    }

    /**
     * 获取控制器
     *
     * @return 控制器
     */
    protected CircuitBreakerHandler getHandler() {
        return new CircuitBreakerHandler();
    }
}
