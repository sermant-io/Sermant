/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adaptor.sentinel;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.huawei.apm.core.lubanops.integration.enums.HttpMethod;
import com.huawei.flowcontrol.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.adapte.cse.match.BusinessMatcher;
import com.huawei.flowcontrol.adapte.cse.match.MatchGroupResolver;
import com.huawei.flowcontrol.adapte.cse.resolver.AbstractResolver;
import com.huawei.flowcontrol.adapte.cse.resolver.RateLimitingRuleResolver;
import com.huawei.flowcontrol.adapte.cse.rule.RateLimitingRule;

import java.util.Collection;
import java.util.Map;

/**
 * 流控实体
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class FlowRuleEntity extends AbstractSentinelRuleEntity<FlowRule> {
    private final FlowRule rule;

    private final BusinessMatcher businessMatcher;

    public FlowRuleEntity(FlowRule rule, BusinessMatcher businessMatcher) {
        this.rule = rule;
        this.businessMatcher = businessMatcher;
    }

    @Override
    public boolean match(String url, Map<String, String> headers, HttpMethod method) {
        /*final AbstractResolver<?> resolver = ResolverManager.INSTANCE.getResolver(
                AbstractResolver.getConfigKeyPrefix(RateLimitingRuleResolver.CONFIG_KEY));
        final Map<String, ?> rules = resolver.getRules();

        final AbstractResolver<?> matchResolver = ResolverManager.INSTANCE.getResolver(
                AbstractResolver.getConfigKeyPrefix(MatchGroupResolver.CONFIG_KEY));
        final Map<String, ?> matchRules = matchResolver.getRules();
        for (Map.Entry<String, ?> entry : rules.entrySet()) {
            final BusinessMatcher matcher = (BusinessMatcher) matchRules.get(entry.getKey());
            if (matcher.match(url, headers, method)) {
                return true;
            }
        }*/
        return businessMatcher.match(url, headers, method.toString());
    }

    @Override
    public FlowRule getRule() {
        return rule;
    }

    @Override
    public String getResource() {
        final String resource = rule.getResource();
        if (resource == null) {
            return super.getResource();
        }
        return rule.getResource();
    }
}
