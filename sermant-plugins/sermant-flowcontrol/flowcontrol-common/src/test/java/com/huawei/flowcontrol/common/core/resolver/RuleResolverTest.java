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

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * RuleResolverTest
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class RuleResolverTest {
    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    @After
    public void tearDown() throws Exception {
        operationManagerMockedStatic.close();
    }

    /**
     * test rule resolution configuration notification
     */
    @Test
    public void test() {
        testResolver(new RateLimitingRuleResolver());
        testResolver(new BulkheadRuleResolver());
        testResolver(new CircuitBreakerRuleResolver());
        testResolver(new RetryResolver());
        testResolver(new FaultRuleResolver());
        testResolver(new InstanceIsolationRuleResolver());
    }

    private void testResolver(AbstractResolver<?> resolver) {
        String key = "test";
        resolver.registerListener((updateKey, rules) -> {
            Assert.assertEquals(updateKey, key);
            Assert.assertTrue(rules.isEmpty());
        });
        resolver.notifyListeners(key);
    }
}
