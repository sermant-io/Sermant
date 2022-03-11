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

package com.huawei.flowcontrol.retry.handler;

import com.huawei.flowcontrol.common.adapte.cse.resolver.RetryResolver;
import com.huawei.flowcontrol.common.adapte.cse.rule.RetryRule;
import com.huawei.flowcontrol.common.handler.AbstractRequestHandler;
import com.huawei.flowcontrol.common.handler.retry.RetryContext;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import java.util.HashSet;
import java.util.function.Predicate;

/**
 * 基于resilience4j重试
 *
 * @author zhouss
 * @since 2022-02-18
 */
public class RetryHandlerV2 extends AbstractRequestHandler<Retry, RetryRule> {
    @Override
    protected Retry createProcessor(String businessName, RetryRule rule) {
        final com.huawei.flowcontrol.common.handler.retry.Retry retry = RetryContext.INSTANCE.getRetry();
        if (retry == null) {
            return null;
        }
        return RetryRegistry.of(
            RetryConfig.custom()
                .maxAttempts(rule.getMaxAttempts())
                .retryOnResult(buildRetryResult(retry, rule))
                .retryExceptions(retry.retryExceptions())
                .intervalFunction(getIntervalFunction(rule))
                .build())
            .retry(businessName);
    }

    private Predicate<Object> buildRetryResult(com.huawei.flowcontrol.common.handler.retry.Retry retry,
        RetryRule rule) {
        return result -> retry.needRetry(new HashSet<>(rule.getRetryOnResponseStatus()), result);
    }

    private IntervalFunction getIntervalFunction(RetryRule rule) {
        if (RetryRule.STRATEGY_RANDOM_BACKOFF.equals(rule.getRetryStrategy())) {
            return IntervalFunction.ofExponentialRandomBackoff(rule.getParsedInitialInterval(),
                rule.getMultiplier(), rule.getRandomizationFactor());
        }
        return IntervalFunction.of(rule.getParsedWaitDuration());
    }

    @Override
    protected String configKey() {
        return RetryResolver.CONFIG_KEY;
    }
}
