/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.strategy;

import io.sermant.router.config.entity.Rule;
import io.sermant.router.config.strategy.AbstractRuleStrategy;
import io.sermant.router.spring.strategy.mapper.AbstractMetadataMapper;
import io.sermant.router.spring.strategy.mapper.DefaultMetadataMapper;
import io.sermant.router.spring.strategy.mapper.EurekaMetadataMapper;
import io.sermant.router.spring.strategy.mapper.ZookeeperMetadataMapper;
import io.sermant.router.spring.strategy.rule.InstanceRuleStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Routing Policy handler
 *
 * @author provenceee
 * @since 2021-10-14
 */
public enum RuleStrategyHandler {
    /**
     * Instance
     */
    INSTANCE();

    private final Map<String, AbstractMetadataMapper<Object>> map;

    private final DefaultMetadataMapper defaultMetadataMapper;

    private volatile AbstractRuleStrategy<Object> ruleStrategy;

    RuleStrategyHandler() {
        map = new HashMap<>();
        defaultMetadataMapper = new DefaultMetadataMapper();
        init(new EurekaMetadataMapper());
        init(new ZookeeperMetadataMapper());
    }

    private void init(AbstractMetadataMapper<Object> mapper) {
        for (String name : mapper.getName()) {
            map.put(name, mapper);
        }
    }

    /**
     * Select the instance of route matching
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param rule Routing rules
     * @return Instances of route matching
     */
    public List<Object> getFlowMatchInstances(String serviceName, List<Object> instances, Rule rule) {
        return getRuleStrategy(instances).getFlowMatchInstances(serviceName, instances, rule);
    }

    /**
     * Select an instance that matches the rule
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param rule rule
     * @return The instance of the rule match
     */
    public List<Object> getMatchInstances(String serviceName, List<Object> instances, Rule rule) {
        return getRuleStrategy(instances).getMatchInstances(serviceName, instances, rule);
    }

    /**
     * Select the instance of route matching
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param tags Label
     * @return Instances of route matching
     */
    public List<Object> getMatchInstancesByRequest(String serviceName, List<Object> instances,
            Map<String, String> tags) {
        return getRuleStrategy(instances).getMatchInstancesByRequest(serviceName, instances, tags);
    }

    /**
     * Select instances of mismatched labels
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @param tags Label
     * @param isReturnAllInstancesWhenMismatch If there is no match, whether to return all instances
     * @return Instances that are route-filtered
     */
    public List<Object> getMismatchInstances(String serviceName, List<Object> instances,
            List<Map<String, String>> tags, boolean isReturnAllInstancesWhenMismatch) {
        return getRuleStrategy(instances)
                .getMismatchInstances(serviceName, instances, tags, isReturnAllInstancesWhenMismatch);
    }

    private AbstractRuleStrategy<Object> getRuleStrategy(List<Object> instances) {
        if (ruleStrategy == null) {
            synchronized (RuleStrategyHandler.class) {
                if (ruleStrategy == null) {
                    ruleStrategy = new InstanceRuleStrategy<>(getMetadataMapper(instances.get(0)));
                }
            }
        }
        return ruleStrategy;
    }

    private AbstractMetadataMapper<Object> getMetadataMapper(Object obj) {
        return map.getOrDefault(obj.getClass().getName(), defaultMetadataMapper);
    }
}
