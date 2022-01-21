/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.util;

import com.huawei.flowcontrol.adapte.cse.rule.isolate.IsolateThreadException;
import com.huawei.flowcontrol.adapte.cse.rule.isolate.IsolateThreadRule;
import com.huawei.flowcontrol.common.entity.FixedResult;
import com.huawei.flowcontrol.common.enums.FlowControlEnum;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
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
    public static final String FLOW_RESULT = "Flow limited";

    /**
     * 降级提示信息
     */
    public static final String DEGRADE_RESULT = "Degraded and blocked";

    /**
     * 隔离仓异常
     */
    public static final String ISOLATE_RESULT = "Exceeded the max concurrent calls!";

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
        } else if (rule instanceof IsolateThreadRule) {
            return ISOLATE_RESULT;
        } else {
            return DEFAULT_RESULT;
        }
    }

    public static void handleBlockException(BlockException blockException, FixedResult fixedResult) {
        if (blockException instanceof FlowException) {
            fixedResult.setResult(FlowControlEnum.RATE_LIMITED);
        } else if (blockException instanceof DegradeException) {
            fixedResult.setResult(FlowControlEnum.CIRCUIT_BREAKER);
        } else if (blockException instanceof IsolateThreadException) {
            fixedResult.setResult(FlowControlEnum.BULKHEAD_FULL);
        } else {
            return;
        }
    }
}
