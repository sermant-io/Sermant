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

package com.huawei.flowcontrol.res4j.handler;

import com.huawei.flowcontrol.common.core.resolver.SystemRuleResolver;
import com.huawei.flowcontrol.common.core.rule.SystemRule;
import com.huawei.flowcontrol.common.handler.AbstractRequestHandler;
import com.huawei.flowcontrol.res4j.exceptions.SystemRuleFault;

import java.util.Optional;

/**
 * 系统流控处理器
 *
 * @author xuezechao1
 * @since 2022-12-06
 */
public class SystemRuleHandler extends AbstractRequestHandler<SystemRuleFault, SystemRule> {

    @Override
    protected Optional<SystemRuleFault> createProcessor(String businessName, SystemRule rule) {
        return Optional.of(new SystemRuleFault(rule));
    }

    @Override
    protected String configKey() {
        return SystemRuleResolver.CONFIG_KEY;
    }
}
