/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.loadbalancer;

import com.huawei.loadbalancer.config.LoadbalancerConfig;
import com.huawei.loadbalancer.config.RibbonLoadbalancerType;
import com.huawei.loadbalancer.interceptor.RibbonLoadBalancerInterceptor;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RetryRule;
import com.netflix.loadbalancer.RoundRobinRule;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 测试BaseLoadBalancer chooseServer方法的拦截点
 *
 * @author provenceee
 * @see com.netflix.loadbalancer.BaseLoadBalancer
 * @since 2022-03-01
 */
@SuppressWarnings("checkstyle:all")
public class RibbonLoadBalancerInterceptorTest {
    private final LoadbalancerConfig config;

    private final RibbonLoadBalancerInterceptor interceptor;

    private final ExecuteContext context;

    private final BaseLoadBalancer loadBalancer;

    /**
     * 构造方法
     */
    public RibbonLoadBalancerInterceptorTest() throws NoSuchFieldException, IllegalAccessException {
        interceptor = new RibbonLoadBalancerInterceptor();
        config = new LoadbalancerConfig();
        Field field = interceptor.getClass().getDeclaredField("config");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(interceptor, config);
        loadBalancer = new BaseLoadBalancer();
        context = ExecuteContext.forMemberMethod(loadBalancer, null, null, null, null);
    }

    @Test
    public void test() {
        // 测试配置为null
        RibbonLoadBalancerInterceptor nullConfigInterceptor = new RibbonLoadBalancerInterceptor();
        nullConfigInterceptor.before(context);
        Assert.assertEquals(loadBalancer, context.getObject());
        Assert.assertEquals(RoundRobinRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

        // 测试负载均衡策略为null
        config.setRibbonType(null);
        interceptor.before(context);
        Assert.assertEquals(loadBalancer, context.getObject());
        Assert.assertEquals(RoundRobinRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

        // 测试负载均衡器类型一致
        config.setRibbonType(RibbonLoadbalancerType.RETRY);
        loadBalancer.setRule(new RetryRule());
        interceptor.before(context);
        Assert.assertEquals(loadBalancer, context.getObject());
        Assert.assertEquals(RetryRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());

        // 测试把重试换成随机
        config.setRibbonType(RibbonLoadbalancerType.RANDOM);
        interceptor.before(context);
        Assert.assertEquals(loadBalancer, context.getObject());
        Assert.assertEquals(RandomRule.class, ((BaseLoadBalancer) context.getObject()).getRule().getClass());
    }
}