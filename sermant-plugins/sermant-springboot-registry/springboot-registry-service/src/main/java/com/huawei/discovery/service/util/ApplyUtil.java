/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.discovery.service.util;

import com.huawei.discovery.entity.Recorder;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.retry.Retry;
import com.huawei.discovery.retry.RetryException;
import com.huawei.discovery.service.lb.DiscoveryManager;
import com.huawei.discovery.service.lb.LbConstants;
import com.huawei.discovery.service.lb.stats.InstanceStats;
import com.huawei.discovery.service.lb.stats.ServiceStatsManager;
import com.huawei.discovery.service.retry.policy.PolicyContext;
import com.huawei.discovery.service.retry.policy.RetryPolicy;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Added exception information collection
 *
 * @author zhp
 * @since 2023-03-08
 */
public class ApplyUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private ApplyUtil() {
    }

    /**
     * Service calls
     *
     * @param invokeFunc Call the method
     * @param invokerContext Context
     * @return Invoke the result
     */
    public static Object apply(Function<InvokerContext, Object> invokeFunc, InvokerContext invokerContext) {
        return invokeFunc.apply(invokerContext);
    }

    /**
     * Service calls
     *
     * @param invokeFunc The specific call is implemented
     * @param serviceName The name of the service
     * @param retry Retry information
     * @param invokerContext The context in which the service is called
     * @param retryPolicy Retry policy
     * @return Invoke the result
     * @throws Exception Invoke the exception message
     */
    public static Optional<Object> invokeWithEx(Function<InvokerContext, Object> invokeFunc, String serviceName,
            Retry retry, InvokerContext invokerContext, RetryPolicy retryPolicy) throws Exception {
        final Retry.RetryContext<Recorder> context = retry.context();
        final PolicyContext policyContext = new PolicyContext();
        boolean isInRetry = false;
        do {
            final long start = System.currentTimeMillis();
            policyContext.setServiceInstance(invokerContext.getServiceInstance());
            final Optional<ServiceInstance> instance = choose(serviceName, isInRetry, policyContext, retryPolicy);
            if (!instance.isPresent()) {
                LOGGER.warning("Can not find provider service named : " + serviceName);
                return Optional.empty();
            }
            invokerContext.setServiceInstance(instance.get());
            final InstanceStats stats = ServiceStatsManager.INSTANCE.getInstanceStats(instance.get());
            context.onBefore(stats);
            long consumeTimeMs;
            try {
                final Object result = ApplyUtil.apply(invokeFunc, invokerContext);
                consumeTimeMs = System.currentTimeMillis() - start;
                isInRetry = true;
                if (invokerContext.getEx() != null) {
                    // If there is an exception in the call, it will be returned as an exception
                    context.onError(stats, invokerContext.getEx(), consumeTimeMs);
                    invokerContext.setEx(null);
                    continue;
                }
                final boolean isNeedRetry = context.onResult(stats, result, consumeTimeMs);
                if (!isNeedRetry) {
                    context.onComplete(stats);
                    return Optional.ofNullable(result);
                }
            } catch (RetryException ex) {
                handleEx(ex, context, stats, System.currentTimeMillis() - start);
            }
        } while (true);
    }

    /**
     * Exception handling
     *
     * @param ex Exception information
     * @param context Contextual information
     * @param stats Instance metric data
     * @param consumeTimeMs Time consumed
     * @throws RetryException Service call exception information
     */
    private static void handleEx(Exception ex, Retry.RetryContext<Recorder> context, InstanceStats stats,
            long consumeTimeMs) throws RetryException {
        if (ex instanceof RetryException) {
            throw (RetryException) ex;
        }
        context.onError(stats, ex, consumeTimeMs);
    }

    /**
     * Service Selection
     *
     * @param serviceName The name of the service
     * @param isInRetry Whether to try again
     * @param policyContext The context of the retry policy
     * @param retryPolicy Retry policy
     * @return Service selection results
     */
    private static Optional<ServiceInstance> choose(String serviceName, boolean isInRetry, PolicyContext policyContext,
                                                    RetryPolicy retryPolicy) {
        if (isInRetry) {
            final Optional<ServiceInstance> select = retryPolicy.select(serviceName, policyContext);
            select.ifPresent(instance -> LOGGER.info(String.format(Locale.ENGLISH,
                    "Start retry for invoking instance [id: %s] of service [%s] at time %s",
                    instance.getMetadata().get(LbConstants.SERMANT_DISCOVERY), serviceName, LocalDateTime.now())));
            return select;
        }
        return DiscoveryManager.INSTANCE.choose(serviceName);
    }
}