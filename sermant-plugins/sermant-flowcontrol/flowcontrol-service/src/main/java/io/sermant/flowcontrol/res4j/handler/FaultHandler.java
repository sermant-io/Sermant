/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.flowcontrol.res4j.handler;

import io.sermant.flowcontrol.common.core.constants.RuleConstants;
import io.sermant.flowcontrol.common.core.resolver.FaultRuleResolver;
import io.sermant.flowcontrol.common.core.rule.fault.AbortFault;
import io.sermant.flowcontrol.common.core.rule.fault.DelayFault;
import io.sermant.flowcontrol.common.core.rule.fault.Fault;
import io.sermant.flowcontrol.common.core.rule.fault.FaultRule;
import io.sermant.flowcontrol.common.handler.AbstractRequestHandler;

import java.util.Optional;

/**
 * isolation bin handler
 *
 * @author zhouss
 * @since 2022-01-24
 */
public class FaultHandler extends AbstractRequestHandler<Fault, FaultRule> {
    @Override
    protected final Optional<Fault> createHandler(String businessName, FaultRule rule) {
        if (RuleConstants.FAULT_RULE_DELAY_TYPE.equals(rule.getType())) {
            return Optional.of(new DelayFault(rule));
        }
        return Optional.of(new AbortFault(rule));
    }

    @Override
    protected final String configKey() {
        return FaultRuleResolver.CONFIG_KEY;
    }
}
