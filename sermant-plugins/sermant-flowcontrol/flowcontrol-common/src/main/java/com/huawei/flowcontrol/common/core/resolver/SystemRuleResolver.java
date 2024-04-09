/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.common.core.resolver;

import com.huawei.flowcontrol.common.core.rule.SystemRule;

/**
 * system flow control parsing class
 *
 * @author xuezechao1
 * @since 2022-12-05
 */
public class SystemRuleResolver extends AbstractResolver<SystemRule> {

    /**
     * system rule flow control configuration key
     */
    public static final String CONFIG_KEY = "servicecomb.system";

    /**
     * system flow control analytic constructor
     */
    public SystemRuleResolver() {
        super(CONFIG_KEY);
    }

    @Override
    protected Class<SystemRule> getRuleClass() {
        return SystemRule.class;
    }
}
