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

import com.huawei.flowcontrol.common.core.constants.RuleConstants;
import com.huawei.flowcontrol.common.core.rule.fault.FaultRule;

import org.junit.Assert;

/**
 * 错误注入解析器测试
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class FaultRuleResolverTest extends AbstractRuleResolverTest<FaultRule> {
    private static final long CONFIG_DELAY_TIME = 5000L;

    private static final int CONFIG_PERCENT = 50;

    private static final String CONFIG_FALLBACK_TYPE = RuleConstants.FAULT_RULE_FALLBACK_NULL_TYPE;

    private static final String CONFIG_TYPE = RuleConstants.FAULT_RULE_DELAY_TYPE;

    @Override
    public AbstractResolver<FaultRule> getResolver() {
        return new FaultRuleResolver();
    }

    @Override
    public String getConfigKey() {
        return FaultRuleResolver.CONFIG_KEY;
    }

    @Override
    public String getValue() {
        return "type: delay\n"
                + "percentage: 50\n"
                + "fallbackType: ReturnNull\n"
                + "forceClosed: true\n"
                + "delayTime: 5S";
    }

    @Override
    public void checkAttrs(FaultRule rule) {
        Assert.assertEquals(CONFIG_DELAY_TIME, rule.getParsedDelayTime());
        Assert.assertEquals(CONFIG_PERCENT, rule.getPercentage());
        Assert.assertEquals(CONFIG_FALLBACK_TYPE, rule.getFallbackType());
        Assert.assertEquals(CONFIG_TYPE, rule.getType());
        Assert.assertTrue(rule.isForceClosed());
    }
}
