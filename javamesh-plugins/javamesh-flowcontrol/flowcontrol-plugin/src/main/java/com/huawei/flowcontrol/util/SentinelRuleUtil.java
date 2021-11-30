/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.util;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

/**
 * sentinel资源达到阈值后返回消息工具类
 *
 * @author liyi
 * @since 2020-08-30
 */
public class SentinelRuleUtil {
    /**
     * 流控提示信息
     */
    public static final String FLOW_RESULT = "flow limited";

    /**
     * 降级提示信息
     */
    public static final String DEGRADE_RESULT = "Degraded and blocked";

    /**
     * 默认提示信息
     */
    public static final String DEFAULT_RESULT = "Controlled by flow";

    private SentinelRuleUtil() {
    }

    /**
     * 达到阈值后返回提示信息
     *
     * @param rule AbstractRule
     * @return 提示信息
     */
    public static String getResult(AbstractRule rule) {
        if (rule instanceof FlowRule) {
            return FLOW_RESULT;
        } else if (rule instanceof DegradeRule) {
            return DEGRADE_RESULT;
        } else {
            return DEFAULT_RESULT;
        }
    }
}
