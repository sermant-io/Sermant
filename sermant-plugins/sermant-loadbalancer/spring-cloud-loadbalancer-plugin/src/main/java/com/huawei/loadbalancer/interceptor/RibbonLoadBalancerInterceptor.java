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

import com.huawei.loadbalancer.cache.RibbonLoadbalancerCache;

import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.config.RibbonLoadbalancerType;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.loadbalancer.utils.RibbonUtils;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.BestAvailableRule;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.ResponseTimeWeightedRule;
import com.netflix.loadbalancer.RetryRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.WeightedResponseTimeRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ribbon BaseLoadBalancer负载均衡增强类
 *
 * @author provenceee
 * @since 2022-02-24
 */
public class RibbonLoadBalancerInterceptor extends AbstractInterceptor {
    /**
     * 是否备份原生的负载均衡类型
     */
    private final AtomicBoolean isBackUp = new AtomicBoolean();

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
        this.config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LbContext.INSTANCE.setCurLoadbalancerType(LbContext.LOADBALANCER_RIBBON);
        setRule(context);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private void setRule(ExecuteContext context) {
        if (!RuleManager.INSTANCE.isConfigured()) {
            return;
        }
        final Object rawLoadbalancerKey = context.getArguments()[0];
        if (rawLoadbalancerKey != null && !(rawLoadbalancerKey instanceof String)) {
            return;
        }
        String loadbalancerKey = (String) rawLoadbalancerKey;
        final Optional<String> serviceNameOptional = RibbonUtils.resolveServiceNameByKey(loadbalancerKey);
        if (!serviceNameOptional.isPresent()) {
            return;
        }
        String serviceName = serviceNameOptional.get();
        final RibbonLoadbalancerType targetType = RibbonLoadbalancerCache.INSTANCE.getTargetServiceLbType(serviceName);
        if (targetType != null) {
            doSet(context.getObject(), map.get(targetType));
            return;
        }
        final Optional<RibbonLoadbalancerType> matchType = getRibbonLoadbalancerType(serviceName);
        if (matchType.isPresent()) {
            final AbstractLoadBalancerRule rule = map.get(matchType.get());
            doSet(context.getObject(), rule);
            RibbonLoadbalancerCache.INSTANCE.put(serviceName, matchType.get());
        } else {
            useDefaultType(serviceName, context);
        }
    }

    private void useDefaultType(String serviceName, ExecuteContext context) {
        final String defaultRule = config.getDefaultRule();
        if (defaultRule == null) {
            return;
        }
        RibbonLoadbalancerType.matchLoadbalancer(defaultRule).ifPresent(ribbonLoadbalancerType -> {
            final AbstractLoadBalancerRule rule = map.get(ribbonLoadbalancerType);
            doSet(context.getObject(), rule);
            RibbonLoadbalancerCache.INSTANCE.put(serviceName, ribbonLoadbalancerType);
        });
    }

    private void doSet(Object obj, AbstractLoadBalancerRule rule) {
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) obj;
        backUp(loadBalancer);
        if (loadBalancer.getRule().getClass() == rule.getClass()) {
            // 如果原来的负载均衡器跟需要的一样，就不需要修改了，直接return，不影响原方法
            return;
        }
        loadBalancer.setRule(rule);
    }

    private void backUp(BaseLoadBalancer loadBalancer) {
        if (!isBackUp.compareAndSet(false, true)) {
            return;
        }
        final IRule rule = loadBalancer.getRule();
        for (Entry<RibbonLoadbalancerType, AbstractLoadBalancerRule> entry : map.entrySet()) {
            if (entry.getValue().getClass() == rule.getClass()) {
                RibbonLoadbalancerCache.INSTANCE.setOriginType(entry.getKey());
                break;
            }
        }
    }

    private Optional<RibbonLoadbalancerType> getRibbonLoadbalancerType(String serviceName) {
        final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE
                .getTargetServiceRule(serviceName);
        if (!targetServiceRule.isPresent()) {
            return Optional.empty();
        }
        return RibbonLoadbalancerType
                .matchLoadbalancer(targetServiceRule.get().getRule());
    }
}
