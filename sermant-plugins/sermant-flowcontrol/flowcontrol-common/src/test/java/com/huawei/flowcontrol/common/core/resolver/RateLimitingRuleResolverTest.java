/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huawei.flowcontrol.common.core.rule.RateLimitingRule;

import org.junit.Assert;

/**
 * Analysis of rate limiting rules
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class RateLimitingRuleResolverTest extends AbstractRuleResolverTest<RateLimitingRule> {
    private static final long CONFIG_LIMIT_REFRESH_PERIOD = 1000L;

    private static final long CONFIG_RATE = 999L;

    @Override
    public AbstractResolver<RateLimitingRule> getResolver() {
        return new RateLimitingRuleResolver();
    }

    @Override
    public String getConfigKey() {
        return RateLimitingRuleResolver.CONFIG_KEY;
    }

    @Override
    public String getValue() {
        return "limitRefreshPeriod: \"1000\"\n"
                + "name: flow\n"
                + "rate: \"999\"";
    }

    @Override
    public void checkAttrs(RateLimitingRule rule) {
        Assert.assertEquals(CONFIG_LIMIT_REFRESH_PERIOD, rule.getParsedLimitRefreshPeriod());
        Assert.assertEquals(CONFIG_RATE, rule.getRate());
    }
}
