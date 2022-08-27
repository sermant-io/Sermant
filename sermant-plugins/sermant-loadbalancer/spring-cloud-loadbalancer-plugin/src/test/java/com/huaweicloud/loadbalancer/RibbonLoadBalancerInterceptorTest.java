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

package com.huaweicloud.loadbalancer;

import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.config.RibbonLoadbalancerType;
import com.huaweicloud.loadbalancer.interceptor.RibbonLoadBalancerInterceptor;
import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RetryRule;
import com.netflix.loadbalancer.RoundRobinRule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * 测试BaseLoadBalancer chooseServer方法的拦截点
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
     * 配置转换器
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
     * 构造方法
     */
    public RibbonLoadBalancerInterceptorTest() {
        loadBalancer = new BaseLoadBalancer();
        context = ExecuteContext.forMemberMethod(loadBalancer, null, new Object[] {null}, null, null);
    }

    @Test
    public void test() {
        // 测试配置为null
        RibbonLoadBalancerInterceptor nullConfigInterceptor = new RibbonLoadBalancerInterceptor();
        nullConfigInterceptor.before(context);
        Assert.assertEquals(loadBalancer, context.getObject());
        Assert.assertEquals(RoundRobinRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

        // 测试未配置负载均衡的场景
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)){
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LoadbalancerConfig.class))
                    .thenReturn(new LoadbalancerConfig());
            RibbonLoadBalancerInterceptor interceptor = new RibbonLoadBalancerInterceptor();

            // 无匹配负载均衡场景
            interceptor.before(context);
            Assert.assertEquals(loadBalancer, context.getObject());
            Assert.assertEquals(RoundRobinRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

            // 测试负载均衡器类型一致
            RuleManagerHelper.publishRule(loadBalancer.getName(), RibbonLoadbalancerType.RETRY.getMapperName());
            loadBalancer.setRule(new RetryRule());
            interceptor.before(context);
            Assert.assertEquals(loadBalancer, context.getObject());
            Assert.assertEquals(RetryRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

            // 测试把重试换成随机
            RuleManagerHelper.publishRule(loadBalancer.getName(), RibbonLoadbalancerType.RANDOM.getMapperName());
            interceptor.before(context);
            Assert.assertEquals(loadBalancer, context.getObject());
            Assert.assertEquals(RandomRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());
        }
    }
}
