/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adaptor.sentinel;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.huawei.apm.core.lubanops.integration.enums.HttpMethod;

import java.util.Map;

/**
 * 流控实体
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class FlowRuleEntity extends AbstractSentinelRuleEntity<FlowRule> {
    private FlowRule rule;

    @Override
    public boolean match(Map<String, String> headers, HttpMethod method) {
        return false;
    }

    @Override
    public FlowRule getRule() {
        return rule;
    }
}
