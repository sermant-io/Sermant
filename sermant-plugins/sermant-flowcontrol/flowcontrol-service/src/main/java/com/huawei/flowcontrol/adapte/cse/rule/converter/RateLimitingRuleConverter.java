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

import com.huawei.flowcontrol.common.adapte.cse.rule.RateLimitingRule;
import com.huawei.flowcontrol.common.config.CommonConst;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.Collections;
import java.util.List;

/**
 * 限流规则转换
 *
 * @author zhouss
 * @since 2022-01-21
 */
public class RateLimitingRuleConverter implements RuleConverter<RateLimitingRule, FlowRule> {
    @Override
    public List<FlowRule> convertToSentinelRule(RateLimitingRule resilienceRule) {
        final FlowRule flowRule = new FlowRule();

        // 转换为rate/s, sentinel当前只能以1S为单位进行统计, 因此此处做一定请求比例转换
        flowRule.setCount(resilienceRule.getRate() * CommonConst.RATE_DIV_POINT
            / resilienceRule.getParsedLimitRefreshPeriod());
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setResource(resilienceRule.getName());
        return Collections.singletonList(flowRule);
    }
}
