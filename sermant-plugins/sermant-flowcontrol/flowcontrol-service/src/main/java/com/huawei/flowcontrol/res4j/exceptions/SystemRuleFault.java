/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.res4j.exceptions;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.core.rule.SystemRule;
import com.huawei.flowcontrol.common.core.rule.fault.Fault;
import com.huawei.flowcontrol.res4j.util.SystemRuleUtils;

/**
 * 处理限流异常
 *
 * @author xuezechao1
 * @since 2022-12-07
 */
public class SystemRuleFault implements Fault {
    private SystemRule rule;

    /**
     * 系统流控错误
     *
     * @param systemRule 系统规则
     */
    public SystemRuleFault(SystemRule systemRule) {
        this.rule = systemRule;
    }

    @Override
    public void acquirePermission() {
        // check qps
        if (SystemRuleUtils.getQps() > rule.getQps()) {
            throw new SystemRuleException("Trigger qps flow control", rule);
        }

        // check threadNum
        if (SystemRuleUtils.getThreadNum() > rule.getThreadNum()) {
            throw new SystemRuleException("Trigger threadNum flow control", rule);
        }

        // check rt
        if (SystemRuleUtils.getAveRt() > rule.getAveRt()) {
            throw new SystemRuleException("Trigger rt flow control", rule);
        }

        // check load
        if (SystemRuleUtils.getCurrentLoad() > rule.getSystemLoad()) {
            if (!SystemRuleUtils.isEnableSystemAdaptive() || checkHistoryData()) {
                throw new SystemRuleException("Trigger load flow control", rule);
            }
        }

        // check cpu
        if (SystemRuleUtils.getCurrentCpuUsage() > rule.getCpuUsage()) {
            throw new SystemRuleException("Trigger cpu flow control", rule);
        }
    }

    /**
     * 系统负载自适应检测
     *
     * @return 是否流控
     */
    private boolean checkHistoryData() {
        long threadNum = SystemRuleUtils.getThreadNum();
        if (threadNum > 1 && threadNum > SystemRuleUtils.getQps()
                * SystemRuleUtils.getMinRt() / CommonConst.S_MS_UNIT) {
            return true;
        }
        return false;
    }
}
