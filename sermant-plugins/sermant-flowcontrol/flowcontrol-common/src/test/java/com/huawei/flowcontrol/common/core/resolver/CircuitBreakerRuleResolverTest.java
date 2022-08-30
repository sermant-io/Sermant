/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.core.resolver;

import com.huawei.flowcontrol.common.core.rule.CircuitBreakerRule;

import org.junit.Assert;

/**
 * 熔断规则解析测试
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class CircuitBreakerRuleResolverTest extends AbstractRuleResolverTest<CircuitBreakerRule> {
    private static final int CONFIG_FAILURE_RATE = 80;

    private static final int CONFIG_MIN_NUM_CALLS = 2;

    private static final int CONFIG_SLIDING_WINDOW_SIZE = 10000;

    private static final String CONFIG_SLIDING_WINDOW_TYPE = "time";

    private static final int CONFIG_SLOW_CALL_DURATION_THRESHOLD = 100;

    private static final int CONFIG_SLOW_CALL_RATE_THRESHOLD = 60;

    private static final long CONFIG_WAIT_DURATION_IN_OPEN_STATE = 10000L;

    private static final double DELTA = 1 >> 6;

    @Override
    public AbstractResolver<CircuitBreakerRule> getResolver() {
        return new CircuitBreakerRuleResolver();
    }

    @Override
    public String getConfigKey() {
        return CircuitBreakerRuleResolver.CONFIG_KEY;
    }

    @Override
    public String getValue() {
        return "failureRateThreshold: 80\n"
                + "minimumNumberOfCalls: 2\n"
                + "name: 熔断\n"
                + "slidingWindowSize: 10000\n"
                + "slidingWindowType: time\n"
                + "slowCallDurationThreshold: \"100\"\n"
                + "slowCallRateThreshold: 60\n"
                + "waitDurationInOpenState: 10s";
    }

    @Override
    public void checkAttrs(CircuitBreakerRule rule) {
        Assert.assertEquals(CONFIG_FAILURE_RATE, rule.getFailureRateThreshold(), DELTA);
        Assert.assertEquals(CONFIG_MIN_NUM_CALLS, rule.getMinimumNumberOfCalls());
        Assert.assertEquals(CONFIG_SLIDING_WINDOW_SIZE, rule.getParsedSlidingWindowSize());
        Assert.assertEquals(CONFIG_SLIDING_WINDOW_TYPE, rule.getSlidingWindowType());
        Assert.assertEquals(CONFIG_SLOW_CALL_DURATION_THRESHOLD, rule.getParsedSlowCallDurationThreshold());
        Assert.assertEquals(CONFIG_SLOW_CALL_RATE_THRESHOLD, rule.getSlowCallRateThreshold(), DELTA);
        Assert.assertEquals(CONFIG_WAIT_DURATION_IN_OPEN_STATE, rule.getParsedWaitDurationInOpenState());
    }
}
