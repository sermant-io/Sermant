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

package io.sermant.flowcontrol.res4j.chain.handler;

import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.core.rule.fault.Fault;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;
import io.sermant.flowcontrol.res4j.exceptions.SystemRuleFault;
import io.sermant.flowcontrol.res4j.handler.SystemRuleHandler;
import io.sermant.flowcontrol.res4j.util.SystemRuleUtils;
import io.sermant.flowcontrol.res4j.windows.WindowsArray;

import java.util.List;

/**
 * system rule flow control Handler
 *
 * @author xuezechao1
 * @since 2022-12-05
 */
public class SystemServerReqHandler extends FlowControlHandler<Fault> {
    private final SystemRuleHandler systemRuleHandler = new SystemRuleHandler();

    private final String contextName = SystemServerReqHandler.class.getName();

    @Override
    public void onBefore(RequestContext context, FlowControlScenario businessEntity) {
        if (SystemRuleUtils.isEnableSystemRule()) {
            // flow control detection
            final List<SystemRuleFault> faults =
                    systemRuleHandler.createOrGetHandlers(businessEntity.getMatchedScenarioNames());
            if (!faults.isEmpty()) {
                faults.forEach(Fault::acquirePermission);
            }

            // record request time
            context.save(CommonConst.REQUEST_START_TIME, System.currentTimeMillis());
            WindowsArray.INSTANCE.addThreadNum(context.get(CommonConst.REQUEST_START_TIME, long.class));
        }
        super.onBefore(context, businessEntity);
    }

    @Override
    public void onThrow(RequestContext context, FlowControlScenario scenarioInfo, Throwable throwable) {
        context.remove(getContextName());
        context.remove(CommonConst.REQUEST_START_TIME);
        super.onThrow(context, scenarioInfo, throwable);
    }

    @Override
    public void onResult(RequestContext context, FlowControlScenario scenarioInfo, Object result) {
        if (SystemRuleUtils.isEnableSystemRule() && context.hasKey(CommonConst.REQUEST_START_TIME)) {
            long startTime = context.get(CommonConst.REQUEST_START_TIME, long.class);
            WindowsArray.INSTANCE.addSuccess(startTime);
            WindowsArray.INSTANCE.decreaseThreadNum(startTime);
            WindowsArray.INSTANCE.addRt(startTime, System.currentTimeMillis() - startTime);
            context.remove(CommonConst.REQUEST_START_TIME);
        }
        context.remove(getContextName());
        super.onResult(context, scenarioInfo, result);
    }

    @Override
    protected RequestType direct() {
        return RequestType.SERVER;
    }

    @Override
    public int getOrder() {
        return HandlerConstants.SYSTEM_RULE_FLOW_CONTROL;
    }

    @Override
    public String getContextName() {
        return contextName;
    }
}
