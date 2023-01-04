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
import com.huawei.flowcontrol.common.core.resolver.SystemRuleResolver;
import com.huawei.flowcontrol.common.core.resolver.SystemRuleResolverTest;
import com.huawei.flowcontrol.common.core.rule.AbstractRule;
import com.huawei.flowcontrol.common.core.rule.BulkheadRule;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

/**
 * 规则工具类测试
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class RuleUtilsTest {
    private ResolverManager instance;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void init() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());

        instance = ResolverManager.INSTANCE;
        final Map<String, AbstractResolver<?>> resolversMap = instance.getResolversMap();
        resolversMap.put(BulkheadRuleResolver.CONFIG_KEY + ".", new BulkheadRuleResolver());
        resolversMap.put(CircuitBreakerRuleResolver.CONFIG_KEY + ".", new CircuitBreakerRuleResolver());
        resolversMap.put(InstanceIsolationRuleResolver.CONFIG_KEY + ".", new InstanceIsolationRuleResolver());
        resolversMap.put(FaultRuleResolver.CONFIG_KEY + ".", new FaultRuleResolver());
        resolversMap.put(RateLimitingRuleResolver.CONFIG_KEY + ".", new RateLimitingRuleResolver());
        resolversMap.put(RetryResolver.CONFIG_KEY + ".", new RetryResolver());
        resolversMap.put(SystemRuleResolver.CONFIG_KEY + "." , new SystemRuleResolver());
    }

    @After
    public void tearDown() throws Exception {
        operationManagerMockedStatic.close();
    }

    /**
     * 测试工具类获取规则
     */
    @Test
    public void testGetRule() {
        testResolve();
        final BulkheadRuleResolverTest bulkheadRuleResolverTest = new BulkheadRuleResolverTest();
        final BulkheadRule rule = RuleUtils
                .getRule(bulkheadRuleResolverTest.getBusinessKey(), bulkheadRuleResolverTest.getConfigKey(),
                        BulkheadRule.class);
        Assert.assertNotNull(rule);
    }

    @Test
    public void testResolve() {
        testTargetResolver(new BulkheadRuleResolverTest());
        testTargetResolver(new InstanceIsolationRuleResolverTest());
        testTargetResolver(new CircuitBreakerRuleResolverTest());
        testTargetResolver(new FaultRuleResolverTest());
        testTargetResolver(new RetryResolverTest());
        testTargetResolver(new RateLimitingRuleResolverTest());
        testTargetResolver(new SystemRuleResolverTest());
    }

    private <T extends AbstractRule> void testTargetResolver(AbstractRuleResolverTest<T> resolverTest) {
        instance.resolve(resolverTest.getKey(), resolverTest.getValue(), false);
        resolverTest.checkAttrs((T) instance.getResolver(resolverTest.getConfigKey())
                .getRules().get(resolverTest.getBusinessKey()));
        Assert.assertTrue(instance.hasMatchedRule(resolverTest.getBusinessKey()));
    }
}
