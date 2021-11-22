/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adaptor;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.huawei.flowcontrol.adapte.cse.match.BusinessMatcher;
import com.huawei.flowcontrol.adapte.cse.rule.RateLimitingRule;
import com.huawei.flowcontrol.adaptor.sentinel.FlowRuleEntity;

/**
 * 线上（CSE）流控规则适配
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class RateLimitingRuleAdapter implements CseRuleAdapter<RateLimitingRule, BusinessMatcher, FlowRuleEntity> {
    @Override
    public FlowRuleEntity adapt(RateLimitingRule rateLimitingRule, BusinessMatcher matcher) {
        if (rateLimitingRule == null || matcher == null) {
            return null;
        }
        final FlowRule flowRule = new FlowRule();
        return null;
    }
}
