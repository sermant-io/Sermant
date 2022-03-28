/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.adapte.cse.test;

import com.huawei.flowcontrol.common.adapte.cse.entity.CseServiceMeta;
import com.huawei.flowcontrol.common.adapte.cse.match.BusinessMatcher;
import com.huawei.flowcontrol.common.adapte.cse.match.MatchGroupResolver;
import com.huawei.flowcontrol.common.adapte.cse.resolver.BulkheadRuleResolver;
import com.huawei.flowcontrol.common.adapte.cse.resolver.CircuitBreakerRuleResolver;
import com.huawei.flowcontrol.common.adapte.cse.resolver.RateLimitingRuleResolver;
import com.huawei.flowcontrol.common.adapte.cse.resolver.RetryResolver;
import com.huawei.flowcontrol.common.adapte.cse.rule.BulkheadRule;
import com.huawei.flowcontrol.common.adapte.cse.rule.CircuitBreakerRule;
import com.huawei.flowcontrol.common.adapte.cse.rule.RateLimitingRule;
import com.huawei.flowcontrol.common.adapte.cse.rule.RetryRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 规则解析测试
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class RuleResolveTest {
    @Before
    public void before() {
        CseServiceMeta.getInstance().setServiceName("helloService");
    }

    @Test
    public void resolveRateLimitingRuleTest() {
        String source = "timeoutDuration: 10S\n" +
                "limitRefreshPeriod: 1000S\n" +
                "rate: 1\n" +
                "services: helloService";
        final RateLimitingRuleResolver rateLimitingRuleResolver = new RateLimitingRuleResolver();
        final RateLimitingRule userLoginAction = rateLimitingRuleResolver
                .parseRule("userLoginAction", source, false, false);
        Assert.assertEquals(userLoginAction.getRate(), 1L);
        Assert.assertEquals(userLoginAction.getServices(), "helloService");
        Assert.assertEquals(userLoginAction.getParsedTimeoutDuration(), 10000L);
        Assert.assertEquals(userLoginAction.getParsedLimitRefreshPeriod(), 1000000L);
    }

    @Test
    public void resolveBulkheadRuleTest() {
        String source = "maxConcurrentCalls: 1000 # 最大并发数\n" +
                "maxWaitDuration: 110 # 最大等待时间，默认单位为ms，支持秒为100S\n" +
                "services: helloService";
        final BulkheadRule userLoginAction = new BulkheadRuleResolver()
                .parseRule("userLoginAction", source, false, false);
        Assert.assertEquals(userLoginAction.getMaxConcurrentCalls(), 1000);
        Assert.assertEquals(userLoginAction.getServices(), "helloService");
        Assert.assertEquals(userLoginAction.getParsedMaxWaitDuration(), 110000);
    }

    @Test
    public void resolveCircuitBreakerRuleTest() {
        String source = "failureRateThreshold: 50 # 错误率，达到该错误率触发熔断\n" +
                "slowCallRateThreshold: 100 # 慢调用率\n" +
                "slowCallDurationThreshold: 60000 # 慢调用阈值，即达到60000ms则为慢调用\n" +
                "minimumNumberOfCalls: 100 # 最小调用请求基数\n" +
                "slidingWindowType: count # 滑动窗口类型，请求数（count）与时间（time）\n" +
                "slidingWindowSize: 1000 # 滑动窗口大小，支持秒(S),分钟(M)\n" +
                "services: helloService";
        final CircuitBreakerRuleResolver circuitBreakerRuleResolver = new CircuitBreakerRuleResolver();
        final CircuitBreakerRule userLoginAction = circuitBreakerRuleResolver
                .parseRule("userLoginAction", source, false, false);
        Assert.assertEquals(userLoginAction.getFailureRateThreshold(), 50f, 5f);
        Assert.assertEquals(userLoginAction.getSlowCallRateThreshold(), 100f, 5f);
        Assert.assertEquals(userLoginAction.getParsedSlowCallDurationThreshold(), 60000L);
        Assert.assertEquals(userLoginAction.getMinimumNumberOfCalls(), 100);
        Assert.assertEquals(userLoginAction.getSlidingWindowType(), "count");
        Assert.assertEquals(userLoginAction.getParsedSlidingWindowSize(), 1000L);
        Assert.assertEquals(userLoginAction.getServices(), "helloService");
    }

    @Test
    public void resolveRetryRuleTest() {
        String source = "maxAttempts: \"10\"\n" +
                "name: test-retry\n" +
                "retryOnResponseStatus:\n" +
                "  - \"500\"\n" +
                "  - \"502\"\n" +
                "  - \"501\"\n" +
                "retryStrategy: FixedInterval\n" +
                "waitDuration: \"10S\"\n" +
                "services: helloService";
        final RetryRule userLoginAction = new RetryResolver().parseRule("userLoginAction", source, false, false);
        Assert.assertEquals(userLoginAction.getMaxAttempts(), 10);
        Assert.assertTrue(userLoginAction.getRetryOnResponseStatus().contains("502"));
        Assert.assertEquals(userLoginAction.getParsedWaitDuration(), 10000L);
        Assert.assertEquals(userLoginAction.getRetryStrategy(), "FixedInterval");
        Assert.assertEquals(userLoginAction.getServices(), "helloService");
    }

    @Test
    public void resolveMatchGroupTest() {
        String source = "services: helloService\n" +
                "matches:\n" +
                "  - apiPath:\n" +
                "      prefix: /login\n" +
                "    headers:\n" +
                "      key1:\n" +
                "        prefix: b\n" +
                "      key2:\n" +
                "        contains: c\n" +
                "      key3:\n" +
                "        compare: '>8'\n" +
                "    method:\n" +
                "      - GET\n" +
                "      - PUT\n" +
                "      - POST\n" +
                "      - DELETE\n" +
                "      - PATCH\n" +
                "    name: rule1\n" +
                "    showAlert: false\n" +
                "    uniqIndex: npkls";
        final BusinessMatcher userLoginAction = new MatchGroupResolver()
                .parseRule("userLoginAction", source, false, false);
        Assert.assertTrue(userLoginAction.getMatches().get(0).getMethod().contains("GET"));
        Assert.assertEquals(userLoginAction.getMatches().get(0).getName(), "rule1");
        Assert.assertTrue(userLoginAction.getMatches().get(0).getHeaders().containsKey("key2"));
    }

}
