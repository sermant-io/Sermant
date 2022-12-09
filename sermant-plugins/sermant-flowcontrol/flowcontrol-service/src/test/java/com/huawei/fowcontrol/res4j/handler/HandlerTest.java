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

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.core.constants.RuleConstants;
import com.huawei.flowcontrol.common.core.rule.BulkheadRule;
import com.huawei.flowcontrol.common.core.rule.CircuitBreakerRule;
import com.huawei.flowcontrol.common.core.rule.RateLimitingRule;
import com.huawei.flowcontrol.common.core.rule.fault.Fault;
import com.huawei.flowcontrol.common.core.rule.fault.FaultRule;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Optional;

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
    private static final String WINDOW_SIZE = "10000";
    private static final String DELAY_TIME = "10000";
    private static final int ERROR_CODE = 503;
    private static final int PERCENTAGE = 1;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
        FlowControlConfig flowControlConfig = new FlowControlConfig();
        flowControlConfig.setEnableStartMonitor(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(()->PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(flowControlConfig);
    }

    // mock 静态方法用完后需要关闭
    @After
    public void tearDown() throws Exception {
        operationManagerMockedStatic.close();
        pluginConfigManagerMockedStatic.close();
    }

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
        testCirHandler(new CircuitBreakerHandler());
    }

    /**
     * 测试实例隔离
     */
    @Test
    public void testInstanceIsolation() {
        testCirHandler(new InstanceIsolationHandler());
    }

    @Test
    public void testFault() {
        final FaultHandler faultHandler = new FaultHandler();
        final FaultRule faultRule = new FaultRule();
        faultRule.setDelayTime(DELAY_TIME);
        faultRule.setErrorCode(ERROR_CODE);
        faultRule.setFallbackType(RuleConstants.FAULT_RULE_FALLBACK_THROW_TYPE);
        faultRule.setType(RuleConstants.FAULT_RULE_ABORT_TYPE);
        faultRule.setForceClosed(true);
        faultRule.setPercentage(PERCENTAGE);
        final Optional<Fault> processor = faultHandler.createProcessor(BUSINESS_NAME, faultRule);
        Assert.assertTrue(processor.isPresent());
        final Fault fault = processor.get();
        final Optional<Object> rule = ReflectUtils.getFieldValue(fault, "rule");
        Assert.assertTrue(rule.isPresent() && rule.get() instanceof FaultRule);
        FaultRule realRule = (FaultRule) rule.get();
        Assert.assertEquals(Long.parseLong(DELAY_TIME), realRule.getParsedDelayTime());
        Assert.assertEquals(ERROR_CODE, realRule.getErrorCode());
        Assert.assertEquals(RuleConstants.FAULT_RULE_FALLBACK_THROW_TYPE, realRule.getFallbackType());
        Assert.assertEquals(RuleConstants.FAULT_RULE_ABORT_TYPE, realRule.getType());
        Assert.assertEquals(PERCENTAGE, realRule.getPercentage());
        Assert.assertTrue(realRule.isForceClosed());
    }

    private void testCirHandler(CircuitBreakerHandler handler) {
        final CircuitBreakerRule circuitBreakerRule = new CircuitBreakerRule();
        circuitBreakerRule.setFailureRateThreshold(FAILURE_RATE_THRESHOLD);
        circuitBreakerRule.setMinimumNumberOfCalls(MIN_CALLS);
        circuitBreakerRule.setSlidingWindowSize(WINDOW_SIZE);
        final CircuitBreaker circuitBreaker = handler.createProcessor(BUSINESS_NAME,
                circuitBreakerRule).get();
        Assert.assertEquals(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold(), FAILURE_RATE_THRESHOLD,
                DELTA);
        Assert.assertEquals(circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls(), MIN_CALLS);
        Assert.assertEquals(circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize(),
                Duration.ofMillis(Integer.parseInt(WINDOW_SIZE)).getSeconds());
    }

}
