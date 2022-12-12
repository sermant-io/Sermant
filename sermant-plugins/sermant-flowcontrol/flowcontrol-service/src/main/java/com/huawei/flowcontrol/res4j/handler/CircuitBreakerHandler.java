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

package com.huawei.flowcontrol.res4j.handler;

import com.huawei.flowcontrol.common.core.resolver.CircuitBreakerRuleResolver;
import com.huawei.flowcontrol.common.core.rule.CircuitBreakerRule;
import com.huawei.flowcontrol.common.entity.MetricEntity;
import com.huawei.flowcontrol.common.handler.AbstractRequestHandler;
import com.huawei.flowcontrol.common.util.StringUtils;
import com.huawei.flowcontrol.res4j.adaptor.CircuitBreakerAdaptor;
import com.huawei.flowcontrol.res4j.service.ServiceCollectorService;
import com.huawei.flowcontrol.res4j.util.MonitorUtils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.Map;
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
        CircuitBreaker circuitBreaker = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                        .failureRateThreshold(rule.getFailureRateThreshold())
                        .slowCallRateThreshold(rule.getSlowCallRateThreshold())
                        .waitDurationInOpenState(Duration.ofMillis(rule.getParsedWaitDurationInOpenState()))
                        .slowCallDurationThreshold(Duration.ofMillis(rule.getParsedSlowCallDurationThreshold()))
                        .permittedNumberOfCallsInHalfOpenState(rule.getPermittedNumberOfCallsInHalfOpenState())
                        .minimumNumberOfCalls(rule.getMinimumNumberOfCalls()).slidingWindowType(slidingWindowType)
                        .slidingWindowSize(getWindowSize(slidingWindowType, rule.getParsedSlidingWindowSize())).build())
                .circuitBreaker(businessName);
        if (MonitorUtils.isStartMonitor()) {
            addEventConsumers(circuitBreaker);
            ServiceCollectorService.CIRCUIT_BREAKER_MAP.putIfAbsent(businessName, circuitBreaker);
        }
        return Optional.of(new CircuitBreakerAdaptor(circuitBreaker, rule));
    }

    /**
     * 增加事件消费处理
     *
     * @param circuitBreaker 熔断器
     */
    private static void addEventConsumers(CircuitBreaker circuitBreaker) {
        Map<String, MetricEntity> monitors = ServiceCollectorService.MONITORS;
        MetricEntity metricEntity = monitors.computeIfAbsent(circuitBreaker.getName(), s -> new MetricEntity());
        metricEntity.setName(circuitBreaker.getName());
        circuitBreaker.getEventPublisher().onError(event -> {
            metricEntity.getFailedFuseRequest().getAndIncrement();
            metricEntity.getFuseRequest().getAndIncrement();
            metricEntity.getFuseTime().getAndAdd(event.getElapsedDuration().toMillis());
        }).onSuccess(event -> {
            metricEntity.getFuseRequest().getAndIncrement();
            metricEntity.getSuccessFulFuseRequest().getAndIncrement();
            metricEntity.getFuseTime().getAndAdd(event.getElapsedDuration().toMillis());
        }).onCallNotPermitted(event -> {
            metricEntity.getPermittedFulFuseRequest().getAndIncrement();
        }).onIgnoredError(event -> {
            metricEntity.getIgnoreFulFuseRequest().getAndIncrement();
            metricEntity.getFuseRequest().getAndIncrement();
            metricEntity.getFuseTime().getAndAdd(event.getElapsedDuration().toMillis());
        }).onSlowCallRateExceeded(event -> metricEntity.getSlowFuseRequest().getAndIncrement());
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
