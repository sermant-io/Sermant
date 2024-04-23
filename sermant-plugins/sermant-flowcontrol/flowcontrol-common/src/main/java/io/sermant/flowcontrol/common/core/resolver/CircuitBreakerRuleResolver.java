/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.flowcontrol.common.core.resolver;

import io.sermant.flowcontrol.common.core.rule.CircuitBreakerRule;

/**
 * circuit breaker configuration analysis
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class CircuitBreakerRuleResolver extends AbstractResolver<CircuitBreakerRule> {
    /**
     * circuit breaker configuration key
     */
    public static final String CONFIG_KEY = "servicecomb.circuitBreaker";

    /**
     * circuit breaker construction
     */
    public CircuitBreakerRuleResolver() {
        super(CONFIG_KEY);
    }

    @Override
    protected Class<CircuitBreakerRule> getRuleClass() {
        return CircuitBreakerRule.class;
    }
}
