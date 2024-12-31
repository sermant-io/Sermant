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
import io.sermant.core.utils.CollectionUtils;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.res4j.adaptor.CircuitBreakerAdaptor;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;
import io.sermant.flowcontrol.res4j.exceptions.CircuitBreakerException;
import io.sermant.flowcontrol.res4j.handler.CircuitBreakerHandler;

import java.util.List;
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
    public void onBefore(RequestContext context, FlowControlScenario flowControlScenario) {
        final List<CircuitBreaker> circuitBreakers =
                circuitBreakerHandler.createOrGetHandlers(flowControlScenario.getMatchedScenarioNames());
        if (!circuitBreakers.isEmpty()) {
            for (CircuitBreaker circuitBreaker : circuitBreakers) {
                checkForceOpen(circuitBreaker);
                checkCircuitBreakerState(circuitBreaker);
            }

            // Use the built-in method to get the time. Since the time of each circuit breaker in the list is the same,
            // take the first one
            context.save(getStartTime(), circuitBreakers.get(0).getCurrentTimestamp());
            context.save(getContextName(), circuitBreakers);
        }
        super.onBefore(context, flowControlScenario);
    }

    private void checkCircuitBreakerState(CircuitBreaker circuitBreaker) {
        final boolean isSuccess = circuitBreaker.tryAcquirePermission();
        if (!isSuccess) {
            throw CircuitBreakerException.createException(circuitBreaker);
        }
    }

    /**
     * In forced open state, throw a circuit breaker exception directly
     *
     * @param circuitBreaker Circuit Breaker
     */
    private void checkForceOpen(CircuitBreaker circuitBreaker) {
        if (circuitBreaker instanceof CircuitBreakerAdaptor) {
            if (((CircuitBreakerAdaptor) circuitBreaker).isForceOpen()) {
                // Force open to throw an exception directly
                throw CircuitBreakerException.createException(circuitBreaker);
            }
        }
    }

    @Override
    public void onThrow(RequestContext context, FlowControlScenario scenario, Throwable throwable) {
        process(context, throwable, null, false);
        super.onThrow(context, scenario, throwable);
    }

    @Override
    public void onResult(RequestContext context, FlowControlScenario scenario, Object result) {
        try {
            process(context, null, result, true);
        } finally {
            context.remove(getContextName());
            context.remove(getStartTime());
        }
        super.onResult(context, scenario, result);
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
    protected boolean isSkip(RequestContext context, FlowControlScenario scenario) {
        return scenario == null || CollectionUtils.isEmpty(scenario.getMatchedScenarioNames())
                || isForceClose(circuitBreakerHandler.createOrGetHandlers(scenario.getMatchedScenarioNames()));
    }

    private boolean isForceClose(List<CircuitBreaker> circuitBreakers) {
        for (CircuitBreaker circuitBreaker : circuitBreakers) {
            if (!(circuitBreaker instanceof CircuitBreakerAdaptor)) {
                continue;
            }
            if (((CircuitBreakerAdaptor) circuitBreaker).isForceClosed()) {
                // Force shutdown skips current processor logic
                return true;
            }
        }
        return false;
    }

    /**
     * Get the name of the current cache context
     *
     * @return the name of the current cache context
     */
    @Override
    protected String getContextName() {
        return CONTEXT_NAME;
    }

    /**
     * Get the start time of the current cache context
     *
     * @return the start time of the current cache context
     */
    protected String getStartTime() {
        return START_TIME;
    }

    @Override
    public int getOrder() {
        return HandlerConstants.CIRCUIT_BREAKER_ORDER;
    }

    /**
     * Get the controller
     *
     * @return controller
     */
    protected CircuitBreakerHandler getHandler() {
        return new CircuitBreakerHandler();
    }
}
