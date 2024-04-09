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

package com.huaweicloud.sermant.router.config.entity;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Routing labels
 *
 * @author provenceee
 * @since 2021-10-27
 */
public class RouterConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * The outer key is the type of the label rule, the inner key is the service name, and the inner value is the
     * specific rule of the label route
     */
    private final Map<String, Map<String, List<Rule>>> rules = new ConcurrentHashMap<>();

    /**
     * For a global tag rule, the key is the type of the tag rule and the value is the specific rule of the tag route
     */
    private final Map<String, List<Rule>> globalRules = new ConcurrentHashMap<>();

    /**
     * Obtain routing rule information
     *
     * @return Routing rule information
     */
    public Map<String, Map<String, List<Rule>>> getRouteRule() {
        return rules;
    }

    /**
     * Obtain global rule information
     *
     * @return Global routing rule information
     */
    public Map<String, List<Rule>> getGlobalRule() {
        return globalRules;
    }

    /**
     * Update routing rules at the service granularity
     *
     * @param serviceName Service name
     * @param entireRules Configure the list of routing rules delivered by the center
     */
    public void updateServiceRule(String serviceName, List<EntireRule> entireRules) {
        Map<String, List<Rule>> flowRules = rules.computeIfAbsent(RouterConstant.FLOW_MATCH_KIND,
                key -> new ConcurrentHashMap<>());
        flowRules.remove(serviceName);
        Map<String, List<Rule>> tagRules = rules.computeIfAbsent(RouterConstant.TAG_MATCH_KIND,
                key -> new ConcurrentHashMap<>());
        tagRules.remove(serviceName);
        Map<String, List<Rule>> laneRules = rules.computeIfAbsent(RouterConstant.LANE_MATCH_KIND,
                key -> new ConcurrentHashMap<>());
        laneRules.remove(serviceName);
        for (EntireRule entireRule : entireRules) {
            if (RouterConstant.FLOW_MATCH_KIND.equals(entireRule.getKind())) {
                flowRules.putIfAbsent(serviceName, entireRule.getRules());
                LOGGER.info(String.format(Locale.ROOT, "Flow match rule for %s has been updated: %s ", serviceName,
                        JSONObject.toJSONString(entireRule.getRules())));
                continue;
            }
            if (RouterConstant.TAG_MATCH_KIND.equals(entireRule.getKind())) {
                tagRules.putIfAbsent(serviceName, entireRule.getRules());
                LOGGER.info(String.format(Locale.ROOT, "Tag match rule for %s has been updated: %s ", serviceName,
                        JSONObject.toJSONString(entireRule.getRules())));
                continue;
            }
            if (RouterConstant.LANE_MATCH_KIND.equals(entireRule.getKind())) {
                laneRules.putIfAbsent(serviceName, entireRule.getRules());
                LOGGER.info(String.format(Locale.ROOT, "Lane match rule for %s has been updated: %s ", serviceName,
                        JSONObject.toJSONString(entireRule.getRules())));
            }
        }
    }

    /**
     * Update routing rules at service granularity by type
     *
     * @param serviceName Service name
     * @param entireRule Rules
     */
    public void updateServiceRule(String serviceName, EntireRule entireRule) {
        Map<String, List<Rule>> ruleList = rules.computeIfAbsent(entireRule.getKind(),
                key -> new ConcurrentHashMap<>());
        ruleList.put(serviceName, entireRule.getRules());
        LOGGER.info(String.format(Locale.ROOT, "Rule for %s has been updated: %s ", serviceName,
                JSONObject.toJSONString(entireRule)));
    }

    /**
     * Remove the routing rule for the service
     *
     * @param serviceName Service name
     */
    public void removeServiceRule(String serviceName) {
        Map<String, List<Rule>> flowRules = rules.get(RouterConstant.FLOW_MATCH_KIND);
        if (!CollectionUtils.isEmpty(flowRules)) {
            flowRules.remove(serviceName);
        }
        Map<String, List<Rule>> tagRules = rules.get(RouterConstant.TAG_MATCH_KIND);
        if (!CollectionUtils.isEmpty(tagRules)) {
            tagRules.remove(serviceName);
        }
        Map<String, List<Rule>> laneRules = rules.get(RouterConstant.LANE_MATCH_KIND);
        if (!CollectionUtils.isEmpty(laneRules)) {
            laneRules.remove(serviceName);
        }
        LOGGER.info(String.format(Locale.ROOT, "All rules for %s have been removed! ", serviceName));
    }

    /**
     * Remove the routing rule for the service
     *
     * @param serviceName Service name
     * @param kind The type of rule
     */
    public void removeServiceRule(String serviceName, String kind) {
        Map<String, List<Rule>> ruleList = rules.get(kind);
        if (!CollectionUtils.isEmpty(ruleList)) {
            ruleList.remove(serviceName);
        }
        LOGGER.info(String.format(Locale.ROOT, "%s rules for %s have been removed! ", kind, serviceName));
    }

    /**
     * Reset the routing rule
     *
     * @param map Routing rules
     */
    public void resetRouteRule(Map<String, List<EntireRule>> map) {
        rules.clear();
        for (Map.Entry<String, List<EntireRule>> ruleEntry : map.entrySet()) {
            for (EntireRule entireRule : ruleEntry.getValue()) {
                Map<String, List<Rule>> serviceRuleMap = rules.computeIfAbsent(entireRule.getKind(),
                        key -> new ConcurrentHashMap<>());
                serviceRuleMap.putIfAbsent(ruleEntry.getKey(), entireRule.getRules());
            }
        }
    }

    /**
     * Reset the routing rule
     *
     * @param kind The type of rule
     * @param map Routing rules
     */
    public void resetRouteRule(String kind, Map<String, EntireRule> map) {
        if (map == null) {
            return;
        }
        if (map.isEmpty()) {
            rules.remove(kind);
        } else {
            for (Map.Entry<String, EntireRule> ruleEntry : map.entrySet()) {
                EntireRule entireRule = ruleEntry.getValue();
                Map<String, List<Rule>> serviceRuleMap = rules.compute(kind, (key, value) -> new ConcurrentHashMap<>());
                serviceRuleMap.put(ruleEntry.getKey(), entireRule.getRules());
            }
        }
        LOGGER.info(String.format(Locale.ROOT, "Service rules have been updated: %s",
                JSONObject.toJSONString(map)));
    }

    /**
     * Reset the global routing rule
     *
     * @param list Routing rules
     */
    public void resetGlobalRule(List<EntireRule> list) {
        globalRules.clear();
        for (EntireRule entireRule : list) {
            globalRules.put(entireRule.getKind(), entireRule.getRules());
        }
        LOGGER.info(String.format(Locale.ROOT, "Global rules have been updated: %s",
                JSONObject.toJSONString(list)));
    }

    /**
     * Reset the global routing rule
     *
     * @param entireRule Routing rules
     */
    public void resetGlobalRule(EntireRule entireRule) {
        List<Rule> ruleList = entireRule.getRules();
        if (ruleList == null) {
            return;
        }
        if (ruleList.isEmpty()) {
            globalRules.remove(entireRule.getKind());
        } else {
            globalRules.put(entireRule.getKind(), ruleList);
        }
        LOGGER.info(String.format(Locale.ROOT, "Global rules have been updated: %s",
                JSONObject.toJSONString(entireRule)));
    }

    /**
     * Whether the routing rule is invalid
     *
     * @param configuration Routing rules
     * @param kind Type
     * @return Whether it is invalid
     */
    public static boolean isInValid(RouterConfiguration configuration, String kind) {
        if (configuration == null) {
            return true;
        }
        if (!CollectionUtils.isEmpty(configuration.getRouteRule().get(kind))) {
            return false;
        }
        return CollectionUtils.isEmpty(configuration.getGlobalRule().get(kind));
    }
}