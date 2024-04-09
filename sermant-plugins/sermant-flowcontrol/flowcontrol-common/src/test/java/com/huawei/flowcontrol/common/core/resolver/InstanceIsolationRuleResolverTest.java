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

package com.huawei.flowcontrol.common.core.resolver;

import com.huawei.flowcontrol.common.core.rule.CircuitBreakerRule;

/**
 * isolation chamber test
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class InstanceIsolationRuleResolverTest extends CircuitBreakerRuleResolverTest {
    @Override
    public String getConfigKey() {
        return InstanceIsolationRuleResolver.CONFIG_KEY;
    }

    @Override
    public AbstractResolver<CircuitBreakerRule> getResolver() {
        return new InstanceIsolationRuleResolver();
    }
}
