/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.resolver;

import com.huawei.flowcontrol.adapte.cse.rule.CircuitBreakerRule;

/**
 * 隔熔断配置解析
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class CircuitBreakerRuleResolver extends AbstractRuleResolver<CircuitBreakerRule> {
    /**
     * 熔断配置 键
     */
    public static final String CONFIG_KEY = "servicecomb.circuitBreaker";

    public CircuitBreakerRuleResolver() {
        super(CONFIG_KEY);
    }

    @Override
    protected Class<CircuitBreakerRule> getRuleClass() {
        return CircuitBreakerRule.class;
    }
}
