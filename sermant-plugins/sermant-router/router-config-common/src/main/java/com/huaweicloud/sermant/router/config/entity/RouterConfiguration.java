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
 * 路由标签
 *
 * @author provenceee
 * @since 2021-10-27
 */
public class RouterConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    /**
     * 服务的标签规则,外层key为标签规则类型kind，内层key为服务名，内层value为该类型标签路由的具体规则
     */
    private final Map<String, Map<String, List<Rule>>> rules = new ConcurrentHashMap<>();

    /**
     * 全局标签规则,key为标签规则类型kind，value为该类型标签路由的具体规则
     */
    private final Map<String, List<Rule>> globalRules = new ConcurrentHashMap<>();

    public Map<String, Map<String, List<Rule>>> getRouteRule() {
        return rules;
    }

    public Map<String, List<Rule>> getGlobalRule() {
        return globalRules;
    }

    /**
     * 更新服务粒度的路由规则
     *
     * @param serviceName 服务名
     * @param entireRules 配置中心下发的路由规则列表
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
     * 移除服务的路由规则
     *
     * @param serviceName 服务名
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
     * 重置路由规则
     *
     * @param map 路由规则
     */
    public void resetRouteRule(Map<String, List<EntireRule>> map) {
        rules.clear();
        for (String serviceName : map.keySet()) {
            for (EntireRule entireRule : map.get(serviceName)) {
                Map<String, List<Rule>> serviceRuleMap = rules.computeIfAbsent(entireRule.getKind(),
                    key -> new ConcurrentHashMap<>());
                serviceRuleMap.putIfAbsent(serviceName, entireRule.getRules());
            }
        }
    }

    /**
     * 重置全局路由规则
     *
     * @param list 路由规则
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
     * 路由规则是否无效
     *
     * @param configuration 路由规则
     * @return 是否无效
     */
    public static boolean isInValid(RouterConfiguration configuration) {
        return configuration == null || (CollectionUtils.isEmpty(configuration.getRouteRule())
            && CollectionUtils.isEmpty(configuration.getGlobalRule()));
    }
}