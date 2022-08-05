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
 * ribbon loadbalancer负载均衡缓存
 *
 * @author zhouss
 * @since 2022-08-12
 */
public enum RibbonLoadbalancerCache {
    /**
     * 单例
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 新负载均衡缓存
     * <pre>
     *     key: service name
     *     value: 负载均衡类型
     * </pre>
     */
    private final Map<String, Optional<RibbonLoadbalancerType>> newTypeCache = new ConcurrentHashMap<>();

    /**
     * 原始负载均衡;Ribbon负载均衡不会关联服务名, 若用户为配置负载均衡key, 则全局仅一个。若用户已使用自身的负载均衡key, 则不会支持
     */
    private RibbonLoadbalancerType originType;

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
            newTypeCache.put(rule.getServiceName(), Optional.of(originType));
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
            // 恢复
            newTypeCache.put(oldServiceName, Optional.ofNullable(originType));
        }
    }

    /**
     * 存放负载均衡类型
     *
     * @param serviceName 服务名
     * @param loadbalancerType 负载均衡类型
     * @see IRule
     */
    public void put(String serviceName, RibbonLoadbalancerType loadbalancerType) {
        newTypeCache.put(serviceName, Optional.ofNullable(loadbalancerType));
    }

    /**
     * 获取指定服务名的缓存负载均衡类型
     *
     * @param serviceName 缓存
     * @return 负载均衡
     */
    public Optional<RibbonLoadbalancerType> getTargetServiceLbType(String serviceName) {
        final Optional<RibbonLoadbalancerType> ribbonLoadbalancerType = newTypeCache.get(serviceName);
        if (ribbonLoadbalancerType == null || !ribbonLoadbalancerType.isPresent()) {
            // 尝试匹配
            final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE.getTargetServiceRule(serviceName);
            Optional<RibbonLoadbalancerType> matchType;
            if (targetServiceRule.isPresent()) {
                matchType = RibbonLoadbalancerType
                        .matchLoadbalancer(targetServiceRule.get().getRule());
            } else {
                matchType = Optional.of(originType);
            }
            newTypeCache.put(serviceName, matchType);
            return matchType;
        }
        return ribbonLoadbalancerType;
    }

    public void setOriginType(RibbonLoadbalancerType originType) {
        this.originType = originType;
    }
}
