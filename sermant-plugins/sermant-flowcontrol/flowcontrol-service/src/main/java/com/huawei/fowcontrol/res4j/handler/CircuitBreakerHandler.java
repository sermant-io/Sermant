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

package com.huawei.fowcontrol.res4j.handler;

import com.huawei.flowcontrol.common.core.resolver.CircuitBreakerRuleResolver;
import com.huawei.flowcontrol.common.core.rule.CircuitBreakerRule;
import com.huawei.flowcontrol.common.handler.AbstractRequestHandler;
import com.huawei.flowcontrol.common.util.StringUtils;
import com.huawei.fowcontrol.res4j.adaptor.CircuitBreakerAdaptor;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.Optional;

/**
 * 熔断处理器
 *
 * @author zhouss
 * @since 2022-01-24
 */
public class CircuitBreakerHandler extends AbstractRequestHandler<CircuitBreaker, CircuitBreakerRule> {
    @Override
    protected final Optional<CircuitBreaker> createProcessor(String businessName, CircuitBreakerRule rule) {
        final SlidingWindowType slidingWindowType = getSlidingWindowType(rule.getSlidingWindowType());
        return Optional.of(new CircuitBreakerAdaptor(CircuitBreakerRegistry.of(CircuitBreakerConfig
                .custom()
                .failureRateThreshold(rule.getFailureRateThreshold())
                .slowCallRateThreshold(rule.getSlowCallRateThreshold())
                .waitDurationInOpenState(Duration.ofMillis(rule.getParsedWaitDurationInOpenState()))
                .slowCallDurationThreshold(Duration.ofMillis(rule.getParsedSlowCallDurationThreshold()))
                .permittedNumberOfCallsInHalfOpenState(rule.getPermittedNumberOfCallsInHalfOpenState())
                .minimumNumberOfCalls(rule.getMinimumNumberOfCalls())
                .slidingWindowType(slidingWindowType)
                .slidingWindowSize(getWindowSize(slidingWindowType, rule.getParsedSlidingWindowSize()))
                .build())
                .circuitBreaker(businessName), rule));
    }

    private int getWindowSize(SlidingWindowType slidingWindowType, long parsedSlidingWindowSize) {
        if (slidingWindowType == SlidingWindowType.COUNT_BASED) {
            return (int) parsedSlidingWindowSize;
        }

        // rest4j暂且仅支持秒作为时间窗口, 这里 parsedSlidingWindowSize为毫秒, 因此此处需转换成秒
        return (int) Duration.ofMillis(parsedSlidingWindowSize).getSeconds();
    }

    private CircuitBreakerConfig.SlidingWindowType getSlidingWindowType(String type) {
        if (StringUtils.equalIgnoreCase(type, CircuitBreakerRule.SLIDE_WINDOW_COUNT_TYPE)) {
            return CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
        }
        return CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
    }

    @Override
    protected String configKey() {
        return CircuitBreakerRuleResolver.CONFIG_KEY;
    }
}
