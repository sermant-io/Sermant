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

package com.huawei.flowcontrol.common.core;

import static org.junit.Assert.*;

import com.huawei.flowcontrol.common.core.resolver.AbstractResolver;
import com.huawei.flowcontrol.common.core.resolver.AbstractRuleResolverTest;
import com.huawei.flowcontrol.common.core.resolver.BulkheadRuleResolver;
import com.huawei.flowcontrol.common.core.resolver.BulkheadRuleResolverTest;
import com.huawei.flowcontrol.common.core.resolver.CircuitBreakerRuleResolver;
import com.huawei.flowcontrol.common.core.resolver.CircuitBreakerRuleResolverTest;
import com.huawei.flowcontrol.common.core.resolver.FaultRuleResolver;
import com.huawei.flowcontrol.common.core.resolver.FaultRuleResolverTest;
import com.huawei.flowcontrol.common.core.resolver.InstanceIsolationRuleResolver;
import com.huawei.flowcontrol.common.core.resolver.InstanceIsolationRuleResolverTest;
import com.huawei.flowcontrol.common.core.resolver.RateLimitingRuleResolver;
import com.huawei.flowcontrol.common.core.resolver.RateLimitingRuleResolverTest;
import com.huawei.flowcontrol.common.core.resolver.RetryResolver;
import com.huawei.flowcontrol.common.core.resolver.RetryResolverTest;
import com.huawei.flowcontrol.common.core.rule.AbstractRule;
import com.huawei.flowcontrol.common.core.rule.BulkheadRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * 测试规则管理器
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class ResolverManagerTest {
    private final ResolverManager instance = ResolverManager.INSTANCE;

    @Before
    public void init() {
        final Map<String, AbstractResolver<?>> resolversMap = instance.getResolversMap();
        resolversMap.put(BulkheadRuleResolver.CONFIG_KEY + ".", new BulkheadRuleResolver());
        resolversMap.put(CircuitBreakerRuleResolver.CONFIG_KEY + ".", new CircuitBreakerRuleResolver());
        resolversMap.put(InstanceIsolationRuleResolver.CONFIG_KEY + ".", new InstanceIsolationRuleResolver());
        resolversMap.put(FaultRuleResolver.CONFIG_KEY + ".", new FaultRuleResolver());
        resolversMap.put(RateLimitingRuleResolver.CONFIG_KEY + ".", new RateLimitingRuleResolver());
        resolversMap.put(RetryResolver.CONFIG_KEY + ".", new RetryResolver());
    }

    @Test
    public void testIsTarget() {
        Assert.assertTrue(instance.isTarget(BulkheadRuleResolver.CONFIG_KEY + "."));
        Assert.assertTrue(instance.isTarget(CircuitBreakerRuleResolver.CONFIG_KEY + "."));
        Assert.assertTrue(instance.isTarget(InstanceIsolationRuleResolver.CONFIG_KEY + "."));
        Assert.assertTrue(instance.isTarget(FaultRuleResolver.CONFIG_KEY + "."));
        Assert.assertTrue(instance.isTarget(RateLimitingRuleResolver.CONFIG_KEY + "."));
        Assert.assertTrue(instance.isTarget(RetryResolver.CONFIG_KEY + "."));
    }

    /**
     * 测试解析
     */
    @Test
    public void testResolve() {
        testTargetResolver(new BulkheadRuleResolverTest());
        testTargetResolver(new InstanceIsolationRuleResolverTest());
        testTargetResolver(new CircuitBreakerRuleResolverTest());
        testTargetResolver(new FaultRuleResolverTest());
        testTargetResolver(new RetryResolverTest());
        testTargetResolver(new RateLimitingRuleResolverTest());
    }

    private <T extends AbstractRule> void testTargetResolver(AbstractRuleResolverTest<T> resolverTest) {
        instance.resolve(resolverTest.getKey(), resolverTest.getValue(), false);
        resolverTest.checkAttrs((T) instance.getResolver(resolverTest.getConfigKey())
                .getRules().get(resolverTest.getBusinessKey()));
        Assert.assertTrue(instance.hasMatchedRule(resolverTest.getBusinessKey()));
    }
}
