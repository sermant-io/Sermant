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

package com.huawei.fowcontrol.res4j.handler;

import com.huawei.flowcontrol.common.adapte.cse.resolver.BulkheadRuleResolver;
import com.huawei.flowcontrol.common.adapte.cse.rule.BulkheadRule;
import com.huawei.flowcontrol.common.handler.AbstractRequestHandler;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

import java.time.Duration;

/**
 * 隔离仓处理器
 *
 * @author zhouss
 * @since 2022-01-24
 */
public class BulkheadHandler extends AbstractRequestHandler<Bulkhead, BulkheadRule> {
    @Override
    protected final Bulkhead createProcessor(String businessName, BulkheadRule rule) {
        final BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(rule.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofMillis(rule.getParsedMaxWaitDuration()))
                .build();
        return BulkheadRegistry.of(config).bulkhead(businessName);
    }

    @Override
    protected final String configKey() {
        return BulkheadRuleResolver.CONFIG_KEY;
    }
}
