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

package com.huaweicloud.loadbalancer.cache;

import com.huaweicloud.loadbalancer.RuleManagerHelper;
import com.huaweicloud.loadbalancer.YamlRuleConverter;
import com.huaweicloud.loadbalancer.config.RibbonLoadbalancerType;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * Ribbon cache test
 *
 * @author zhouss
 * @since 2022-08-16
 */
public class RibbonLoadbalancerCacheTest {
    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    /**
     * configuration converter
     */
    @Before
    public void setUp() {
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(RuleConverter.class))
                .thenReturn(new YamlRuleConverter());
    }

    @After
    public void close() {
        serviceManagerMockedStatic.close();
    }

    /**
     * test
     */
    @Test
    public void test() {
        final RibbonLoadbalancerCache instance = RibbonLoadbalancerCache.INSTANCE;

        // test publish configuration listening
        String serviceName = "test";
        RuleManagerHelper.publishRule(serviceName, RibbonLoadbalancerType.WEIGHTED_RESPONSE_TIME.getMapperName());
        Assert.assertTrue(instance.getTargetServiceLbType(serviceName).isPresent());
        final RibbonLoadbalancerType ribbonLoadbalancerType = instance.getTargetServiceLbType(serviceName).get();
        Assert.assertEquals(ribbonLoadbalancerType, RibbonLoadbalancerType.WEIGHTED_RESPONSE_TIME);

        // Test publish an empty service name to match the global, the situation clears load balancing
        RuleManagerHelper.publishRule(null, RibbonLoadbalancerType.AVAILABILITY_FILTERING.getMapperName());
        final Optional<RibbonLoadbalancerType> targetServiceLbType = instance.getTargetServiceLbType(serviceName);
        Assert.assertTrue(targetServiceLbType.isPresent());
        Assert.assertEquals(targetServiceLbType.get(), RibbonLoadbalancerType.AVAILABILITY_FILTERING);
        final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE.getTargetServiceRule(serviceName);
        Assert.assertTrue(targetServiceRule.isPresent());
        final Optional<RibbonLoadbalancerType> type = RibbonLoadbalancerType
                .matchLoadbalancer(targetServiceRule.get().getRule());
        Assert.assertTrue(type.isPresent());
        Assert.assertEquals(type.get(), RibbonLoadbalancerType.AVAILABILITY_FILTERING);

        // clean up rule
        RuleManagerHelper.deleteRule(targetServiceRule.get().getServiceName(), targetServiceRule.get().getRule());
    }
}
