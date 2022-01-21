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

package com.huawei.flowcontrol.adapte.cse.rule.converter;

import com.huawei.flowcontrol.common.adapte.cse.rule.CircuitBreakerRule;
import com.huawei.flowcontrol.common.config.CommonConst;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

import java.util.ArrayList;
import java.util.List;

/**
 * 熔断规则转换
 *
 * @author zhouss
 * @since 2022-01-21
 */
public class CircuitBreakerRuleConverter implements RuleConverter<CircuitBreakerRule, DegradeRule> {
    @Override
    public List<DegradeRule> convertToSentinelRule(CircuitBreakerRule resilienceRule) {
        final List<DegradeRule> degradeRules = new ArrayList<DegradeRule>(2);

        // 时间窗口基于请求数 暂时不支持
        if (!"count".equals(resilienceRule.getSlidingWindowType())) {
            // 时间窗口基于请求时间 time
            // 1.基于慢调用率的熔断
            degradeRules.add(createRule(true, resilienceRule));

            // 2.基于错误率
            degradeRules.add(createRule(false, resilienceRule));
        }
        return degradeRules;
    }

    private DegradeRule createRule(boolean isSlowRule, CircuitBreakerRule resilienceRule) {
        final DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(resilienceRule.getName());
        degradeRule.setMinRequestAmount(resilienceRule.getMinimumNumberOfCalls());

        // CSE默认均为1分钟
        degradeRule.setTimeWindow(
                (int) (CircuitBreakerRule.DEFAULT_WAIT_DURATION_IN_OPEN_STATUS_MS / CommonConst.S_MS_UNIT));
        degradeRule.setStatIntervalMs((int) resilienceRule.getParsedSlidingWindowSize());
        if (isSlowRule) {
            degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
            degradeRule.setSlowRatioThreshold(resilienceRule.getSlowCallRateThreshold() / CommonConst.PERCENT);
            degradeRule.setCount(resilienceRule.getParsedSlowCallDurationThreshold());
        } else {
            degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
            degradeRule.setCount(resilienceRule.getFailureRateThreshold() / CommonConst.PERCENT);
        }
        return degradeRule;
    }
}
