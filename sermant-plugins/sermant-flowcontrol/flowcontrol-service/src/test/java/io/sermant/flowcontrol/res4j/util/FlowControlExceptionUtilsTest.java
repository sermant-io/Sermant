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

package io.sermant.flowcontrol.res4j.util;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.core.rule.CircuitBreakerRule;
import io.sermant.flowcontrol.common.core.rule.fault.FaultException;
import io.sermant.flowcontrol.common.core.rule.fault.FaultRule;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.res4j.adaptor.CircuitBreakerAdaptor;
import io.sermant.flowcontrol.res4j.exceptions.CircuitBreakerException;
import io.sermant.flowcontrol.res4j.exceptions.InstanceIsolationException;

import org.junit.Assert;
import org.junit.Test;

/**
 * rest4j test
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class FlowControlExceptionUtilsTest {
    private static final String RULE_NAME = "test";

    /**
     * test exception handling
     */
    @Test
    public void testExceptionHandler() {
        final FlowControlResult flowControlResult = new FlowControlResult();
        FlowControlExceptionUtils
                .handleException(BulkheadFullException.createBulkheadFullException(Bulkhead.ofDefaults(RULE_NAME)),
                        flowControlResult);
        Assert.assertEquals("Bulkhead is full and does not permit further calls!",
                flowControlResult.getResponse().getMsg());

        FlowControlExceptionUtils
                .handleException(RequestNotPermitted.createRequestNotPermitted(RateLimiter.ofDefaults(RULE_NAME)),
                        flowControlResult);
        Assert.assertEquals("Rate Limited", flowControlResult.getResponse().getMsg());

        FlowControlExceptionUtils.handleException(
                CircuitBreakerException.createException(CircuitBreaker.ofDefaults(RULE_NAME)),
                flowControlResult);
        Assert.assertTrue(flowControlResult.getResponse().getMsg().contains("and does not permit further calls"));

        final CircuitBreakerRule circuitBreakerRule = new CircuitBreakerRule();
        circuitBreakerRule.setForceOpen(true);
        FlowControlExceptionUtils.handleException(
                CircuitBreakerException.createException(new CircuitBreakerAdaptor(CircuitBreaker.ofDefaults(RULE_NAME),
                        circuitBreakerRule)), flowControlResult);
        Assert.assertTrue(flowControlResult.getResponse().getMsg().contains("has forced open and deny any requests"));

        FlowControlExceptionUtils.handleException(
                new FaultException(CommonConst.INTERVAL_SERVER_ERROR, "aborted by fault", new FaultRule()),
                flowControlResult);
        Assert.assertEquals("aborted by fault", flowControlResult.getResponse().getMsg());

        FlowControlExceptionUtils.handleException(
                InstanceIsolationException.createException(CircuitBreaker.ofDefaults(RULE_NAME)),
                flowControlResult);
        Assert.assertTrue(flowControlResult.getResponse().getMsg().contains("and does not permit further calls"));

        final CircuitBreakerRule instanceRule = new CircuitBreakerRule();
        instanceRule.setForceOpen(true);
        FlowControlExceptionUtils.handleException(
                InstanceIsolationException.createException(new CircuitBreakerAdaptor(CircuitBreaker.ofDefaults(RULE_NAME),
                        instanceRule)), flowControlResult);
        Assert.assertTrue(flowControlResult.getResponse().getMsg().contains("has forced open and deny any requests"));
    }

    /**
     * Determine whether warehouse resources need to be isolated
     */
    @Test
    public void testCheckFlowControlException() {
        Assert.assertTrue(FlowControlExceptionUtils
                .isNeedReleasePermit(RequestNotPermitted.createRequestNotPermitted(RateLimiter.ofDefaults(RULE_NAME))));
        Assert.assertTrue(FlowControlExceptionUtils.isNeedReleasePermit(
                CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults(RULE_NAME))));
        Assert.assertFalse(FlowControlExceptionUtils.isNeedReleasePermit(
                BulkheadFullException.createBulkheadFullException(Bulkhead.ofDefaults(RULE_NAME))));
    }
}
