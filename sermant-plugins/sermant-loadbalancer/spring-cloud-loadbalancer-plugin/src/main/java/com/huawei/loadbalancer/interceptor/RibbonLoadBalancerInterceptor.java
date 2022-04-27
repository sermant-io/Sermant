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

package com.huawei.loadbalancer.interceptor;

import com.huawei.loadbalancer.config.LoadbalancerConfig;
import com.huawei.loadbalancer.config.RibbonLoadbalancerType;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.BestAvailableRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.ResponseTimeWeightedRule;
import com.netflix.loadbalancer.RetryRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.WeightedResponseTimeRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

import java.util.EnumMap;
import java.util.Map;

/**
 * Ribbon BaseLoadBalancer负载均衡增强类
 *
 * @author provenceee
 * @since 2022-02-24
 */
public class RibbonLoadBalancerInterceptor extends AbstractInterceptor {
    private final Map<RibbonLoadbalancerType, AbstractLoadBalancerRule> map;

    private final LoadbalancerConfig config;

    /**
     * 构造方法
     */
    public RibbonLoadBalancerInterceptor() {
        map = new EnumMap<>(RibbonLoadbalancerType.class);
        map.put(RibbonLoadbalancerType.RANDOM, new RandomRule());
        map.put(RibbonLoadbalancerType.ROUND_ROBIN, new RoundRobinRule());
        map.put(RibbonLoadbalancerType.RETRY, new RetryRule());
        map.put(RibbonLoadbalancerType.BEST_AVAILABLE, new BestAvailableRule());
        map.put(RibbonLoadbalancerType.AVAILABILITY_FILTERING, new AvailabilityFilteringRule());
        map.put(RibbonLoadbalancerType.RESPONSE_TIME_WEIGHTED, new ResponseTimeWeightedRule());
        map.put(RibbonLoadbalancerType.ZONE_AVOIDANCE, new ZoneAvoidanceRule());
        map.put(RibbonLoadbalancerType.WEIGHTED_RESPONSE_TIME, new WeightedResponseTimeRule());
        config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        setRule(context.getObject());
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private void setRule(Object obj) {
        if (config == null || config.getRibbonType() == null) {
            // 没有配置的情况下return，不影响原方法
            return;
        }
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) obj;
        RibbonLoadbalancerType ribbonType = config.getRibbonType();
        AbstractLoadBalancerRule rule = map.get(ribbonType);
        if (loadBalancer.getRule().getClass() == rule.getClass()) {
            // 如果原来的负载均衡器跟需要的一样，就不需要修改了，直接return，不影响原方法
            return;
        }
        loadBalancer.setRule(rule);
    }
}