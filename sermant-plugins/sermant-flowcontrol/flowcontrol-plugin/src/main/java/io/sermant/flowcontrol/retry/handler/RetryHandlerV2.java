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

package io.sermant.flowcontrol.retry.handler;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.sermant.core.service.xds.entity.XdsRetryPolicy;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.flowcontrol.common.core.resolver.RetryResolver;
import io.sermant.flowcontrol.common.core.rule.RetryRule;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.handler.AbstractRequestHandler;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;
import io.sermant.flowcontrol.retry.FeignRequestInterceptor.FeignRetry;
import io.sermant.flowcontrol.retry.HttpRequestInterceptor.HttpRetry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * based on resilience4j retry
 *
 * @author zhouss
 * @since 2022-02-18
 */
public class RetryHandlerV2 extends AbstractRequestHandler<Retry, RetryRule> {
    private final RetryPredicateCreator retryPredicateCreator = new DefaultRetryPredicateCreator();

    /**
     * XDS Handler cache, where the Key of the first level is the service name,
     * the Key of the second level is the route name, the Key of the three level is the xdsRetryPolicy, value
     * is Retry instance, The Retry instance is thread-safe can be used to decorate multiple requests
     */
    private final Map<String, Map<String, Map<String, Optional<Retry>>>> xdsHandlers = new ConcurrentHashMap<>();

    /**
     * gets the specified retry handler
     *
     * @param scenario Scenario information for flow control
     * @param xdsRetryPolicy retry policy information
     * @return handler
     */
    public List<Retry> getXdsRetryHandlers(FlowControlScenario scenario, XdsRetryPolicy xdsRetryPolicy) {
        Map<String, Map<String, Optional<Retry>>> serviceRetryHandlers = xdsHandlers.computeIfAbsent(
                scenario.getServiceName(), k -> new HashMap<>());
        Map<String, Optional<Retry>> routeRetryHandlers = serviceRetryHandlers.computeIfAbsent(scenario.getRouteName(),
                k -> new HashMap<>());
        String retryName = xdsRetryPolicy.toString();
        Optional<Retry> retryHandlerOptions = routeRetryHandlers.computeIfAbsent(retryName, s -> {
            // Clear the original handler to prevent the use of the original handler during configuration refresh
            routeRetryHandlers.clear();
            return createHandler(xdsRetryPolicy, retryName);
        });
        return retryHandlerOptions.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private Optional<Retry> createHandler(XdsRetryPolicy xdsRetryPolicy, String businessName) {
        final io.sermant.flowcontrol.common.handler.retry.Retry retry = RetryContext.INSTANCE.getRetry();
        if (retry == null) {
            return Optional.empty();
        }
        if (xdsRetryPolicy.getPerTryTimeout() <= 0 || CollectionUtils.isEmpty(xdsRetryPolicy.getRetryConditions())
                || xdsRetryPolicy.getMaxAttempts() <= 0) {
            return Optional.empty();
        }
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts((int)xdsRetryPolicy.getMaxAttempts())
                .retryOnResult(retryPredicateCreator.createResultPredicate(retry, xdsRetryPolicy))
                .retryOnException(retryPredicateCreator.createExceptionPredicate(retry, xdsRetryPolicy))
                .intervalFunction(IntervalFunction.of(xdsRetryPolicy.getPerTryTimeout()))
                .failAfterMaxAttempts(false)
                .build();
        return Optional.of(RetryRegistry.of(retryConfig).retry(businessName));
    }

    @Override
    protected Optional<Retry> createHandler(String businessName, RetryRule rule) {
        final io.sermant.flowcontrol.common.handler.retry.Retry retry = RetryContext.INSTANCE.getRetry();
        if (retry == null) {
            return Optional.empty();
        }
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(getMaxAttempts(retry, rule.getMaxAttempts()))
                .retryOnResult(retryPredicateCreator.createResultPredicate(retry, rule))
                .retryOnException(retryPredicateCreator.createExceptionPredicate(retry.retryExceptions()))
                .intervalFunction(getIntervalFunction(rule))
                .failAfterMaxAttempts(rule.isFailAfterMaxAttempts())
                .build();
        return Optional.of(RetryRegistry.of(retryConfig).retry(businessName));
    }

    /**
     * Obtain the maximum number of retries. The maximum number of retries in different execution modes is modified, The
     * method for intercepting has already been executed once, so there is no need for +1 here the current interception
     * based approach is{@link FeignRetry}, others are injection
     *
     * @param retry retry type
     * @param maxAttempts maximum retry
     * @return maximum retry
     */
    private int getMaxAttempts(io.sermant.flowcontrol.common.handler.retry.Retry retry, int maxAttempts) {
        if (retry instanceof FeignRetry || retry instanceof HttpRetry) {
            return maxAttempts;
        }
        return maxAttempts + 1;
    }

    @Override
    protected String configKey() {
        return RetryResolver.CONFIG_KEY;
    }

    private IntervalFunction getIntervalFunction(RetryRule rule) {
        if (RetryRule.STRATEGY_RANDOM_BACKOFF.equals(rule.getRetryStrategy())) {
            return IntervalFunction.ofExponentialRandomBackoff(rule.getParsedInitialInterval(),
                    rule.getMultiplier(), rule.getRandomizationFactor());
        }
        return IntervalFunction.of(rule.getParsedWaitDuration());
    }
}
