/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.resolver;

import com.huawei.flowcontrol.adapte.cse.rule.RateLimitingRule;

/**
 * 限流解析类
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class RateLimitingRuleResolver extends AbstractRuleResolver<RateLimitingRule> {
    /**
     * 限流配置 键
     */
    public static final String CONFIG_KEY = "servicecomb.rateLimiting";

    public RateLimitingRuleResolver() {
        super(CONFIG_KEY);
    }

    @Override
    protected Class<RateLimitingRule> getRuleClass() {
        return RateLimitingRule.class;
    }
}
