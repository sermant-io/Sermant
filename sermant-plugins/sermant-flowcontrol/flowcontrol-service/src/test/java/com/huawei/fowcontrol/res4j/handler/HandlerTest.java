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

import com.huawei.flowcontrol.common.core.rule.BulkheadRule;
import com.huawei.flowcontrol.common.core.rule.CircuitBreakerRule;
import com.huawei.flowcontrol.common.core.rule.RateLimitingRule;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.Assert;
import org.junit.Test;

/**
 * 处理器测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class HandlerTest {
    private static final String BUSINESS_NAME = "test";
    private static final int MAX_CALLS = 100;
    private static final String MAX_WAIT_DURATION = "1000";
    private static final int RATE = 100;
    private static final String LIMIT_PERIOD = "1000";
    private static final float FAILURE_RATE_THRESHOLD = 100f;
    private static final int MIN_CALLS = 10;
    private static final double DELTA = 1e-6d;

    /**
     * 测试隔离仓
     */
    @Test
    public void testBulkhead() {
        final BulkheadHandler bulkheadHandler = new BulkheadHandler();
        final BulkheadRule bulkheadRule = new BulkheadRule();
        bulkheadRule.setMaxConcurrentCalls(MAX_CALLS);
        bulkheadRule.setMaxWaitDuration(MAX_WAIT_DURATION);
        final Bulkhead bulkhead = bulkheadHandler.createProcessor(BUSINESS_NAME, bulkheadRule).get();
        Assert.assertEquals(bulkhead.getBulkheadConfig().getMaxConcurrentCalls(), MAX_CALLS);
        Assert.assertEquals(bulkhead.getBulkheadConfig().getMaxWaitDuration().toMillis(),
            Long.parseLong(MAX_WAIT_DURATION));
    }

    /**
     * 测试限流
     */
    @Test
    public void testRateLimiting() {
        final RateLimitingHandler rateLimitingHandler = new RateLimitingHandler();
        final RateLimitingRule rateLimitingRule = new RateLimitingRule();
        rateLimitingRule.setRate(RATE);
        rateLimitingRule.setLimitRefreshPeriod(LIMIT_PERIOD);
        final RateLimiter rateLimiter = rateLimitingHandler.createProcessor(BUSINESS_NAME, rateLimitingRule).get();
        Assert.assertEquals(rateLimiter.getRateLimiterConfig().getLimitForPeriod(), RATE);
        Assert.assertEquals(rateLimiter.getRateLimiterConfig().getLimitRefreshPeriod().toMillis(),
            Long.parseLong(LIMIT_PERIOD));
    }

    /**
     * 测试熔断
     */
    @Test
    public void testCircuitBreaker() {
        final CircuitBreakerHandler circuitBreakerHandler = new CircuitBreakerHandler();
        final CircuitBreakerRule circuitBreakerRule = new CircuitBreakerRule();
        circuitBreakerRule.setFailureRateThreshold(FAILURE_RATE_THRESHOLD);
        circuitBreakerRule.setMinimumNumberOfCalls(MIN_CALLS);
        final CircuitBreaker circuitBreaker = circuitBreakerHandler.createProcessor(BUSINESS_NAME,
            circuitBreakerRule).get();
        Assert.assertEquals(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold(), FAILURE_RATE_THRESHOLD,
            DELTA);
        Assert.assertEquals(circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls(), MIN_CALLS);
    }

}
