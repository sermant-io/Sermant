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

package com.huaweicloud.loadbalancer.cache;

import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.config.RibbonLoadbalancerType;
import com.huaweicloud.loadbalancer.rule.ChangedLoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.loadbalancer.utils.CacheUtils;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import com.netflix.loadbalancer.IRule;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * ribbon loadbalancer load balancing cache
 *
 * @author zhouss
 * @since 2022-08-12
 */
public enum RibbonLoadbalancerCache {
    /**
     * singleton
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * new load balancing cache
     * <pre>
     *     key: service name
     *     value: load balancing type
     * </pre>
     */
    private final Map<String, Optional<RibbonLoadbalancerType>> newTypeCache = new ConcurrentHashMap<>();

    /**
     * raw load balancing cache. key: serviceName, value: loadBalancingType
     */
    private final Map<String, RibbonLoadbalancerType> originTypeCache = new ConcurrentHashMap<>();

    RibbonLoadbalancerCache() {
        RuleManager.INSTANCE.addRuleListener(this::updateCache);
    }

    private void updateCache(LoadbalancerRule rule, DynamicConfigEvent event) {
        if (rule.getRule() == null || !LbContext.INSTANCE.isTargetLb(LbContext.LOADBALANCER_RIBBON)) {
            return;
        }
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            if (Objects.isNull(rule.getServiceName())) {
                newTypeCache.clear();
                return;
            }
            newTypeCache.put(rule.getServiceName(), Optional.ofNullable(originTypeCache.get(rule.getServiceName())));
        } else if (event.getEventType() == DynamicConfigEventType.MODIFY) {
            processModify(rule);
        } else {
            processAdd(rule);
        }
    }

    private void processAdd(LoadbalancerRule rule) {
        final String serviceName = rule.getServiceName();
        if (serviceName == null) {
            newTypeCache.clear();
            return;
        }
        RibbonLoadbalancerType.matchLoadbalancer(rule.getRule())
                .ifPresent(matchType -> newTypeCache.put(serviceName, Optional.of(matchType)));
    }

    private void processModify(LoadbalancerRule rule) {
        final boolean result = CacheUtils.updateCache(newTypeCache, rule);
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
            recoverOldType(oldRule);
            configNewType(newRule);
        } else if (oldRule.getServiceName() == null && newRule.getServiceName() != null) {
            configNewType(newRule);
        }
    }

    private void configNewType(LoadbalancerRule rule) {
        if (rule.getServiceName() == null) {
            return;
        }
        final Optional<RibbonLoadbalancerType> newLoadbalancer = RibbonLoadbalancerType
                .matchLoadbalancer(rule.getRule());
        if (newLoadbalancer.isPresent()) {
            newTypeCache.put(rule.getServiceName(), newLoadbalancer);
        } else {
            final RibbonLoadbalancerType originType = originTypeCache.get(rule.getServiceName());
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Do not support ribbon loadbalancer type: [%s], "
                            + "loadbalancer type will change to origin type [%s]",
                    rule.getRule(), originType == null ? "unKnow" : originType));
            newTypeCache.put(rule.getServiceName(), Optional.ofNullable(originType));
        }
    }

    private void recoverOldType(LoadbalancerRule rule) {
        final String oldServiceName = rule.getServiceName();
        if (oldServiceName != null) {
            // recover
            newTypeCache.put(oldServiceName, Optional.ofNullable(originTypeCache.get(oldServiceName)));
        }
    }

    /**
     * the load balancing type is stored
     *
     * @param serviceName service name
     * @param loadbalancerType load balancing type
     * @see IRule
     */
    public void put(String serviceName, RibbonLoadbalancerType loadbalancerType) {
        newTypeCache.put(serviceName, Optional.ofNullable(loadbalancerType));
    }

    /**
     * Gets the cache load balancing type for the specified service name
     *
     * @param serviceName cache
     * @return load balancing
     */
    public Optional<RibbonLoadbalancerType> getTargetServiceLbType(String serviceName) {
        final Optional<RibbonLoadbalancerType> ribbonLoadbalancerType = newTypeCache.get(serviceName);
        if (ribbonLoadbalancerType == null || !ribbonLoadbalancerType.isPresent()) {
            // try to match
            final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE.getTargetServiceRule(serviceName);
            Optional<RibbonLoadbalancerType> matchType;
            if (targetServiceRule.isPresent()) {
                matchType = RibbonLoadbalancerType
                        .matchLoadbalancer(targetServiceRule.get().getRule());
            } else {
                matchType = Optional.ofNullable(originTypeCache.get(serviceName));
            }
            newTypeCache.put(serviceName, matchType);
            return matchType;
        }
        return ribbonLoadbalancerType;
    }

    /**
     * backup origin type
     *
     * @param serviceName service name
     * @param targetOriginType target origin type
     */
    public void backUpOriginType(String serviceName, RibbonLoadbalancerType targetOriginType) {
        this.originTypeCache.put(serviceName, targetOriginType);
    }
}
