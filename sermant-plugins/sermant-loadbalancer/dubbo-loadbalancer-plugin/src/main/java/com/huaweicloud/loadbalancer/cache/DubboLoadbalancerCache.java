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

import com.huaweicloud.loadbalancer.config.DubboLoadbalancerType;
import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.rule.ChangedLoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * dubbo load balancing cache
 *
 * @author zhouss
 * @since 2022-08-15
 */
public enum DubboLoadbalancerCache {
    /**
     * instance
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * configure the load balancing type
     * <pre>
     *     key: remote-application
     *     value: DubboLoadbalancerType
     * </pre>
     */
    private final Map<String, DubboLoadbalancerType> newCache = new ConcurrentHashMap<>();

    DubboLoadbalancerCache() {
        RuleManager.INSTANCE.addRuleListener(this::updateCache);
    }

    private void updateCache(LoadbalancerRule rule, DynamicConfigEvent event) {
        if (rule.getRule() == null || !LbContext.INSTANCE.isTargetLb(LbContext.LOADBALANCER_DUBBO)) {
            return;
        }
        final Optional<DubboLoadbalancerType> dubboLoadbalancerType = DubboLoadbalancerType
                .matchLoadbalancer(rule.getRule());
        if (!dubboLoadbalancerType.isPresent()) {
            LOGGER.fine(String.format(Locale.ENGLISH, "Can not support dubbo loadbalancer rule: [%s]",
                    rule.getRule()));
            return;
        }
        final String serviceName = rule.getServiceName();
        checkRemoveOld(rule);
        if (serviceName == null) {
            newCache.clear();
            return;
        }
        newCache.remove(serviceName);
    }

    private void checkRemoveOld(LoadbalancerRule rule) {
        if (!(rule instanceof ChangedLoadbalancerRule)) {
            return;
        }
        ChangedLoadbalancerRule changedLoadbalancerRule = (ChangedLoadbalancerRule) rule;
        final String oldServiceName = changedLoadbalancerRule.getOldRule().getServiceName();
        if (Objects.isNull(oldServiceName)) {
            newCache.clear();
            return;
        }
        newCache.remove(oldServiceName);
    }

    public Map<String, DubboLoadbalancerType> getNewCache() {
        return newCache;
    }
}
