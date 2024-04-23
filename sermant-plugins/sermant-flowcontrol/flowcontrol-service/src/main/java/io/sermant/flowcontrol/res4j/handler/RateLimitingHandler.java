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

package io.sermant.flowcontrol.res4j.handler;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.sermant.flowcontrol.common.core.resolver.RateLimitingRuleResolver;
import io.sermant.flowcontrol.common.core.rule.RateLimitingRule;
import io.sermant.flowcontrol.common.handler.AbstractRequestHandler;

import java.time.Duration;
import java.util.Optional;

/**
 * Flow control handler
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class RateLimitingHandler extends AbstractRequestHandler<RateLimiter, RateLimitingRule> {
    @Override
    protected final Optional<RateLimiter> createProcessor(String businessName, RateLimitingRule rule) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(rule.getRate())
                .limitRefreshPeriod(Duration.ofMillis(rule.getParsedLimitRefreshPeriod()))
                .timeoutDuration(Duration.ofMillis(rule.getParsedTimeoutDuration()))
                .build();
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
        return Optional.of(rateLimiterRegistry.rateLimiter(businessName));
    }

    @Override
    protected final String configKey() {
        return RateLimitingRuleResolver.CONFIG_KEY;
    }
}
