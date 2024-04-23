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

package io.sermant.loadbalancer.cache;

import io.sermant.core.service.ServiceManager;
import io.sermant.loadbalancer.config.DubboLoadbalancerType;
import io.sermant.loadbalancer.rule.LoadbalancerRule;
import io.sermant.loadbalancer.rule.RuleManager;
import io.sermant.loadbalancer.service.RuleConverter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * Dubbo Cache Test
 *
 * @author zhouss
 * @since 2022-08-16
 */
public class DubboLoadbalancerCacheTest {
    private MockedStatic<ServiceManager> serviceManagerMockedStatic;
    /**
     * configuration converter
     */
    @Before
    public void setUp() {
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic .when(() -> ServiceManager.getService(RuleConverter.class))
                .thenReturn(new YamlRuleConverter());
    }

    @After
    public void close() {
        serviceManagerMockedStatic.close();
    }

    /**
     * Dubbo Cache Test
     */
    @Test
    public void test() {
        final DubboLoadbalancerCache instance = DubboLoadbalancerCache.INSTANCE;

        // test publish configuration listening
        String serviceName = "test";
        RuleManagerHelper.publishRule(serviceName, DubboLoadbalancerType.RANDOM.getMapperName());
        Assert.assertNull(instance.getNewCache().get(serviceName));

        // Test publish an empty service name to match the global, the situation clears load balancing
        RuleManagerHelper.publishRule(null, DubboLoadbalancerType.SHORTESTRESPONSE.getMapperName());
        Assert.assertNull(instance.getNewCache().get(serviceName));
        final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE.getTargetServiceRule(serviceName);
        Assert.assertTrue(targetServiceRule.isPresent());
        final Optional<DubboLoadbalancerType> type = DubboLoadbalancerType
                .matchLoadbalancer(targetServiceRule.get().getRule());
        Assert.assertTrue(type.isPresent());
        Assert.assertEquals(type.get(), DubboLoadbalancerType.SHORTESTRESPONSE);

        // clean up rule
        RuleManagerHelper.deleteRule(serviceName, targetServiceRule.get().getRule());
    }
}
