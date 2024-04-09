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

package com.huaweicloud.loadbalancer.rule;

import com.huaweicloud.loadbalancer.listener.CacheListener;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.Optional;

/**
 * rule manager
 *
 * @author zhouss
 * @since 2022-08-10
 */
public enum RuleManager {
    /**
     * instance
     */
    INSTANCE;

    private final LoadbalancerRuleResolver loadbalancerRuleRuleResolver = new LoadbalancerRuleResolver();

    /**
     * parsing configuration
     *
     * @param event event
     */
    public void resolve(DynamicConfigEvent event) {
        loadbalancerRuleRuleResolver.resolve(event);
    }

    /**
     * gets the load balancing type of the target service
     *
     * @param serviceName targetServiceName
     * @return LoadbalancerRule
     */
    public Optional<LoadbalancerRule> getTargetServiceRule(String serviceName) {
        return loadbalancerRuleRuleResolver.getTargetServiceRule(serviceName);
    }

    /**
     * add cache listener
     *
     * @param cacheListener Listener
     */
    public void addRuleListener(CacheListener cacheListener) {
        loadbalancerRuleRuleResolver.addListener(cacheListener);
    }

    /**
     * Whether load balancing policies are configured for the host service
     *
     * @return true: The value is configured
     */
    public boolean isConfigured() {
        return loadbalancerRuleRuleResolver.isConfigured();
    }
}
