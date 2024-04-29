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

package io.sermant.loadbalancer;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RetryRule;
import com.netflix.loadbalancer.RoundRobinRule;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.loadbalancer.config.LoadbalancerConfig;
import io.sermant.loadbalancer.config.RibbonLoadbalancerType;
import io.sermant.loadbalancer.interceptor.RibbonLoadBalancerInterceptor;
import io.sermant.loadbalancer.service.RuleConverter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Test the interception point of the BaseLoadBalancer chooseServer method
 *
 * @author provenceee
 * @see com.netflix.loadbalancer.BaseLoadBalancer
 * @since 2022-03-01
 */
public class RibbonLoadBalancerInterceptorTest {
    private final ExecuteContext context;

    private final BaseLoadBalancer loadBalancer;

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
     * construction method
     */
    public RibbonLoadBalancerInterceptorTest() {
        loadBalancer = new BaseLoadBalancer();
        context = ExecuteContext.forMemberMethod(loadBalancer, null, new Object[] {null}, null, null);
    }

    @Test
    public void test() {
        // test: configure is null
        RibbonLoadBalancerInterceptor nullConfigInterceptor = new RibbonLoadBalancerInterceptor();
        nullConfigInterceptor.before(context);
        Assert.assertEquals(loadBalancer, context.getObject());
        Assert.assertEquals(RoundRobinRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

        // test: Scenarios in which load balancing is not configured
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)){
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LoadbalancerConfig.class))
                    .thenReturn(new LoadbalancerConfig());
            RibbonLoadBalancerInterceptor interceptor = new RibbonLoadBalancerInterceptor();

            // no matching load balancing scenario
            interceptor.before(context);
            Assert.assertEquals(loadBalancer, context.getObject());
            Assert.assertEquals(RoundRobinRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

            // the load balancer type is consistent
            RuleManagerHelper.publishRule(loadBalancer.getName(), RibbonLoadbalancerType.RETRY.getMapperName());
            loadBalancer.setRule(new RetryRule());
            interceptor.before(context);
            Assert.assertEquals(loadBalancer, context.getObject());
            Assert.assertEquals(RetryRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

            // tests replace RETRY with RANDOM
            RuleManagerHelper.publishRule(loadBalancer.getName(), RibbonLoadbalancerType.RANDOM.getMapperName());
            interceptor.before(context);
            Assert.assertEquals(loadBalancer, context.getObject());
            Assert.assertEquals(RandomRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());
        }
    }
}
