/*
 * Copyright (C) 2022-2025 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.xds.traffic.management.handler;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.xds.common.entity.FlowControlScenario;
import io.sermant.xds.common.flowcontrol.retry.RetryContext;
import io.sermant.xds.common.flowcontrol.retry.policy.RetryPolicy;

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
public class RetryHandler {
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
     * @return handler
     */
    public List<Retry> getXdsRetryHandlers(FlowControlScenario scenario) {
        RetryPolicy retryPolicy = RetryContext.INSTANCE.getRetryPolicy();
        if (retryPolicy == null) {
            return Collections.EMPTY_LIST;
        }
        Map<String, Map<String, Optional<Retry>>> serviceRetryHandlers = xdsHandlers.computeIfAbsent(
                scenario.getServiceName(), k -> new HashMap<>());
        Map<String, Optional<Retry>> routeRetryHandlers = serviceRetryHandlers.computeIfAbsent(scenario.getRouteName(),
                k -> new HashMap<>());
        String retryName = retryPolicy.getRetryPolicyName();
        Optional<Retry> retryHandlerOptions = routeRetryHandlers.computeIfAbsent(retryName, s -> {
            // Clear the original handler to prevent the use of the original handler during configuration refresh
            routeRetryHandlers.clear();
            return createHandler(retryPolicy,retryName);
        });
        return retryHandlerOptions.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private Optional<Retry> createHandler(RetryPolicy retryPolicy, String businessName) {
        final io.sermant.xds.common.flowcontrol.retry.Retry retry = RetryContext.INSTANCE.getRetry();
        if (retry == null) {
            return Optional.empty();
        }
        if (retryPolicy.getTryTimeout() <= 0 || CollectionUtils.isEmpty(retryPolicy.getRetryConditions())
                || retryPolicy.getMaxAttempts() <= 0) {
            return Optional.empty();
        }
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts((int)retryPolicy.getMaxAttempts())
                .retryOnResult(retryPredicateCreator.createResultPredicate(retry))
                .retryOnException(retryPredicateCreator.createExceptionPredicate(retry))
                .intervalFunction(IntervalFunction.of(retryPolicy.getTryTimeout()))
                .failAfterMaxAttempts(false)
                .build();
        return Optional.of(RetryRegistry.of(retryConfig).retry(businessName));
    }
}
