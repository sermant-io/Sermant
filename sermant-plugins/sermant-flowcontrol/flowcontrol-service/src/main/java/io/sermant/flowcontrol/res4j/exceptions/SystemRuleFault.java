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

package io.sermant.flowcontrol.res4j.exceptions;

import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.core.rule.SystemRule;
import io.sermant.flowcontrol.common.core.rule.fault.Fault;
import io.sermant.flowcontrol.res4j.util.SystemRuleUtils;

/**
 * Handling current limit exceptions
 *
 * @author xuezechao1
 * @since 2022-12-07
 */
public class SystemRuleFault implements Fault {
    private SystemRule rule;

    /**
     * system rule fault
     *
     * @param systemRule system rule
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
     * system load adaptive detection
     *
     * @return Whether to flow control
     */
    private boolean checkHistoryData() {
        long threadNum = SystemRuleUtils.getThreadNum();
        if (threadNum > 1 && threadNum > SystemRuleUtils.getMaxThreadNum()
                * SystemRuleUtils.getMinRt() / CommonConst.S_MS_UNIT) {
            return true;
        }
        return false;
    }
}
