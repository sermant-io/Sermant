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

import com.huawei.flowcontrol.common.adapte.cse.resolver.CircuitBreakerRuleResolver;
import com.huawei.flowcontrol.common.adapte.cse.rule.CircuitBreakerRule;
import com.huawei.flowcontrol.common.handler.AbstractRequestHandler;
import com.huawei.flowcontrol.common.util.StringUtils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;

/**
 * 熔断处理器
 *
 * @author zhouss
 * @since 2022-01-24
 */
public class CircuitBreakerHandler extends AbstractRequestHandler<CircuitBreaker, CircuitBreakerRule> {
    @Override
    protected final CircuitBreaker createProcessor(String businessName, CircuitBreakerRule rule) {
        return CircuitBreakerRegistry
                .of(CircuitBreakerConfig
                        .custom()
                        .failureRateThreshold(rule.getFailureRateThreshold())
                        .slowCallRateThreshold(rule.getSlowCallRateThreshold())
                        .waitDurationInOpenState(Duration.ofMillis(rule.getParsedWaitDurationInOpenState()))
                        .slowCallDurationThreshold(Duration.ofMillis(rule.getParsedSlowCallDurationThreshold()))
                        .permittedNumberOfCallsInHalfOpenState(rule.getPermittedNumberOfCallsInHalfOpenState())
                        .minimumNumberOfCalls(rule.getMinimumNumberOfCalls())
                        .slidingWindowType(getSlidingWindowType(rule.getSlidingWindowType()))
                        .slidingWindowSize(Integer.parseInt(rule.getSlidingWindowSize()))
                        .build())
                .circuitBreaker(businessName);
    }

    private CircuitBreakerConfig.SlidingWindowType getSlidingWindowType(String type) {
        if (StringUtils.equalIgnoreCase(type, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED.toString())) {
            return CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
        }
        return CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
    }

    @Override
    protected final String configKey() {
        return CircuitBreakerRuleResolver.CONFIG_KEY;
    }
}
