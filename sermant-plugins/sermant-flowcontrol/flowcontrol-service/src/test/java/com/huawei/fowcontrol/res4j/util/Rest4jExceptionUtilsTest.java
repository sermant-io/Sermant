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

package com.huawei.fowcontrol.res4j.util;

import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.enums.FlowControlEnum;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.Assert;
import org.junit.Test;

/**
 * rest4j测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class Rest4jExceptionUtilsTest {
    private static final String RULE_NAME = "test";

    /**
     * 测试异常处理
     */
    @Test
    public void testExceptionHandler() {
        final FlowControlResult flowControlResult = new FlowControlResult();
        Rest4jExceptionUtils
            .handleException(BulkheadFullException.createBulkheadFullException(Bulkhead.ofDefaults(RULE_NAME)),
                flowControlResult);
        Assert.assertEquals(FlowControlEnum.BULKHEAD_FULL, flowControlResult.getResult());

        Rest4jExceptionUtils
            .handleException(RequestNotPermitted.createRequestNotPermitted(RateLimiter.ofDefaults(RULE_NAME)),
                flowControlResult);
        Assert.assertEquals(FlowControlEnum.RATE_LIMITED, flowControlResult.getResult());

        Rest4jExceptionUtils.handleException(
            CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults(RULE_NAME)),
            flowControlResult);
        Assert.assertEquals(FlowControlEnum.CIRCUIT_BREAKER, flowControlResult.getResult());
    }

    /**
     * 判断是否需是否隔离仓资源
     */
    @Test
    public void testCheckFlowControlException() {
        Assert.assertTrue(Rest4jExceptionUtils
            .isNeedReleasePermit(RequestNotPermitted.createRequestNotPermitted(RateLimiter.ofDefaults(RULE_NAME))));
        Assert.assertTrue(Rest4jExceptionUtils.isNeedReleasePermit(
            CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults(RULE_NAME))));
        Assert.assertFalse(Rest4jExceptionUtils.isNeedReleasePermit(
            BulkheadFullException.createBulkheadFullException(Bulkhead.ofDefaults(RULE_NAME))));
    }
}
