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

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.loadbalancer.config.LbContext;
import io.sermant.loadbalancer.config.SpringLoadbalancerType;
import io.sermant.loadbalancer.rule.ChangedLoadbalancerRule;
import io.sermant.loadbalancer.rule.LoadbalancerRule;
import io.sermant.loadbalancer.rule.RuleManager;
import io.sermant.loadbalancer.utils.CacheUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.logging.Logger;

/**
 * spring loadbalancer load balancer cache
 *
 * @author provenceee
 * @since 2022-01-20
 */
public enum SpringLoadbalancerCache {
    /**
     * instance
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * parameter cache
     *
     * @see org.springframework.beans.factory.ObjectProvider
     */
    private final Map<String, Object> providerMap = new ConcurrentHashMap<>();

    /**
     * cache of the original host`s load balancer
     */
    private final Map<String, Object> originCache = new ConcurrentHashMap<>();

    /**
     * new load balancer cache
     */
    private final Map<String, Optional<Object>> newCache = new ConcurrentHashMap<>();

    SpringLoadbalancerCache() {
        RuleManager.INSTANCE.addRuleListener(this::updateCache);
    }

    private void updateCache(LoadbalancerRule rule, DynamicConfigEvent event) {
        if (rule.getRule() == null || !LbContext.INSTANCE.isTargetLb(LbContext.LOADBALANCER_SPRING)) {
            return;
        }
        final Optional<SpringLoadbalancerType> springLoadbalancerType = SpringLoadbalancerType
                .matchLoadbalancer(rule.getRule());
        if (!springLoadbalancerType.isPresent()) {
            LOGGER.fine(String.format(Locale.ENGLISH, "Can not support spring loadbalancer rule: [%s]",
                    rule.getRule()));
            return;
        }
        if (event.getEventType() == DynamicConfigEventType.MODIFY) {
            processModify(rule);
        } else {
            final String serviceName = rule.getServiceName();
            if (Objects.isNull(serviceName)) {
                newCache.clear();
                return;
            }
            if (event.getEventType() == DynamicConfigEventType.DELETE) {
                newCache.put(rule.getServiceName(), Optional.ofNullable(originCache.get(rule.getServiceName())));
            } else {
                newCache.remove(serviceName);
            }
        }
    }

    private void processModify(LoadbalancerRule rule) {
        final boolean result = CacheUtils.updateCache(newCache, rule);
        if (!result) {
            return;
        }
        if (!(rule instanceof ChangedLoadbalancerRule)) {
            LOGGER.warning("LoadbalancerRule can not be cast to ChangedLoadbalancerRule");
            return;
        }
        ChangedLoadbalancerRule changedLoadbalancerRule = (ChangedLoadbalancerRule) rule;
        final LoadbalancerRule oldRule = changedLoadbalancerRule.getOldRule();
        final LoadbalancerRule newRule = changedLoadbalancerRule.getNewRule();
        if (oldRule.getServiceName() != null && newRule.getServiceName() != null) {
            newCache.put(oldRule.getServiceName(), Optional.ofNullable(originCache.get(oldRule.getServiceName())));
        }
    }

    /**
     * put provider
     *
     * @param serviceId service id
     * @param provider provider
     */
    public void putProvider(String serviceId, Object provider) {
        providerMap.putIfAbsent(serviceId, provider);
    }

    /**
     * get provider
     *
     * @param serviceId service id
     * @return provider
     */
    public Object getProvider(String serviceId) {
        return providerMap.get(serviceId);
    }

    /**
     * stored in the original load balancer
     *
     * @param serviceId service id
     * @param loadBalancer load balancer
     */
    public void putOrigin(String serviceId, Object loadBalancer) {
        originCache.putIfAbsent(serviceId, loadBalancer);
    }

    /**
     * obtain the original load balancer
     *
     * @param serviceId service id
     * @return load balancer
     */
    public Object getOrigin(String serviceId) {
        return originCache.get(serviceId);
    }

    /**
     * gets the spring load balancing type
     *
     * @param serviceId service id
     * @param creatFun creation method
     * @return load balancing
     */
    public Optional<Object> getTargetServiceLbType(String serviceId,
            BiFunction<SpringLoadbalancerType, String, Optional<Object>> creatFun) {
        final Optional<Object> type = newCache.get(serviceId);
        if (type == null || !type.isPresent()) {
            final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE.getTargetServiceRule(serviceId);
            Optional<Object> loadbalancer;
            if (targetServiceRule.isPresent()) {
                final Optional<SpringLoadbalancerType> springLoadbalancerType = SpringLoadbalancerType
                        .matchLoadbalancer(targetServiceRule.get().getRule());
                if (springLoadbalancerType.isPresent()) {
                    loadbalancer = creatFun.apply(springLoadbalancerType.get(), serviceId);
                } else {
                    loadbalancer = Optional.ofNullable(originCache.get(serviceId));
                }
            } else {
                loadbalancer = Optional.ofNullable(originCache.get(serviceId));
            }
            newCache.put(serviceId, loadbalancer);
            return loadbalancer;
        }
        return type;
    }

    public Map<String, Optional<Object>> getNewCache() {
        return newCache;
    }
}
