/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adaptor;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
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

    /**
     * 一个业务场景一个规则
     * 其中场景名作为资源名
     *
     * @param rateLimitingRule 流控策略
     * @param matcher cse匹配器
     * @return FlowRuleEntity
     */
    @Override
    public FlowRuleEntity adapt(RateLimitingRule rateLimitingRule, BusinessMatcher matcher) {
        if (rateLimitingRule == null || matcher == null) {
            return null;
        }
        final FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(rateLimitingRule.getRate());
        // 流控不支持单位时间统计，默认只有全局的1S时间窗口
        return new FlowRuleEntity(flowRule, matcher);
    }
}
