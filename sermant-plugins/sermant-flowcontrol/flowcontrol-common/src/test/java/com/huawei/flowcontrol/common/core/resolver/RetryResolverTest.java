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

import com.huawei.flowcontrol.common.core.rule.RetryRule;

import org.junit.Assert;

/**
 * retry rule
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class RetryResolverTest extends AbstractRuleResolverTest<RetryRule> {
    private static final long CONFIG_WAIT_DURATION = 2000L;

    private static final String CONFIG_RETRY_STRATEGY = "FixedInterval";

    private static final int CONFIG_MAX_ATTEMPTS = 2;

    private static final String CONFIG_RETRY_ON_RESPONSE_STATUS = "500";

    @Override
    public AbstractResolver<RetryRule> getResolver() {
        return new RetryResolver();
    }

    @Override
    public String getConfigKey() {
        return RetryResolver.CONFIG_KEY;
    }

    @Override
    public String getValue() {
        return "waitDuration: \"2000\"\n"
                + "retryStrategy: FixedInterval\n"
                + "maxAttempts: 2\n"
                + "retryOnResponseStatus:\n"
                + "- 500";
    }

    @Override
    public void checkAttrs(RetryRule rule) {
        Assert.assertEquals(CONFIG_WAIT_DURATION, rule.getParsedWaitDuration());
        Assert.assertEquals(CONFIG_RETRY_STRATEGY, rule.getRetryStrategy());
        Assert.assertEquals(CONFIG_MAX_ATTEMPTS, rule.getMaxAttempts());
        Assert.assertTrue(rule.getRetryOnResponseStatus().contains(CONFIG_RETRY_ON_RESPONSE_STATUS));
    }
}
