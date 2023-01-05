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

package com.huawei.flowcontrol.res4j.chain.handler;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.core.rule.fault.Fault;
import com.huawei.flowcontrol.common.entity.RequestEntity.RequestType;
import com.huawei.flowcontrol.res4j.chain.HandlerConstants;
import com.huawei.flowcontrol.res4j.chain.context.RequestContext;
import com.huawei.flowcontrol.res4j.exceptions.SystemRuleFault;
import com.huawei.flowcontrol.res4j.handler.SystemRuleHandler;
import com.huawei.flowcontrol.res4j.util.SystemRuleUtils;
import com.huawei.flowcontrol.res4j.windows.WindowsArray;

import java.util.List;
import java.util.Set;

/**
 * 系统规则流控处理
 *
 * @author xuezechao1
 * @since 2022-12-05
 */
public class SystemServerReqHandler extends FlowControlHandler<Fault> {
    private final SystemRuleHandler systemRuleHandler = new SystemRuleHandler();

    private final String contextName = SystemServerReqHandler.class.getName();

    @Override
    public void onBefore(RequestContext context, Set<String> businessNames) {
        if (SystemRuleUtils.isEnableSystemRule()) {

            // 流控检测
            final List<SystemRuleFault> faults = systemRuleHandler.createOrGetHandlers(businessNames);
            if (!faults.isEmpty()) {
                faults.forEach(Fault::acquirePermission);
            }

            // 记录请求时间
            context.save(CommonConst.REQUEST_START_TIME, System.currentTimeMillis());
            WindowsArray.INSTANCE.addThreadNum(context.get(CommonConst.REQUEST_START_TIME, long.class));
        }
        super.onBefore(context, businessNames);
    }

    @Override
    public void onThrow(RequestContext context, Set<String> businessNames, Throwable throwable) {
        context.remove(getContextName());
        context.remove(CommonConst.REQUEST_START_TIME);
        super.onThrow(context, businessNames, throwable);
    }

    @Override
    public void onResult(RequestContext context, Set<String> businessNames, Object result) {
        if (SystemRuleUtils.isEnableSystemRule() && context.hasKey(CommonConst.REQUEST_START_TIME)) {
            long startTime = context.get(CommonConst.REQUEST_START_TIME, long.class);
            WindowsArray.INSTANCE.addSuccess(startTime);
            WindowsArray.INSTANCE.decreaseThreadNum(startTime);
            WindowsArray.INSTANCE.addRt(startTime, System.currentTimeMillis() - startTime);
            context.remove(CommonConst.REQUEST_START_TIME);
        }
        context.remove(getContextName());
        super.onResult(context, businessNames, result);
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
