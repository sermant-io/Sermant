/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.entity.ValueMatch;
import com.huaweicloud.sermant.router.config.utils.FlowRuleUtils;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;
import com.huaweicloud.sermant.router.spring.cache.AppCache;
import com.huaweicloud.sermant.router.spring.strategy.RuleStrategyHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流量匹配方式的路由处理器
 *
 * @author lilai
 * @since 2023-02-21
 */
public class FlowRouteHandler extends AbstractRouteHandler {
    private static final String VERSION_KEY = "version";

    private final RouterConfig routerConfig;

    // 用于过滤实例的tags集合，value为null，代表含有该标签的实例全部过滤，不判断value值
    private final Map<String, String> allMismatchTags;

    /**
     * 构造方法
     */
    public FlowRouteHandler() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
        allMismatchTags = new HashMap<>();
        for (String requestTag : routerConfig.getRequestTags()) {
            allMismatchTags.put(requestTag, null);
        }

        // 所有实例都含有version，所以不能存入null值
        allMismatchTags.remove(VERSION_KEY);
    }

    @Override
    public List<Object> handle(String targetName, List<Object> instances, RequestData requestData) {
        if (requestData == null) {
            return super.handle(targetName, instances, null);
        }
        if (!shouldHandle(instances)) {
            return instances;
        }
        List<Object> result = routerConfig.isUseRequestRouter()
                ? getTargetInstancesByRequest(targetName, instances, requestData.getTag())
                : getTargetInstancesByRules(targetName, instances, requestData.getPath(), requestData.getTag());
        return super.handle(targetName, result, requestData);
    }

    @Override
    public int getOrder() {
        return RouterConstant.FLOW_HANDLER_ORDER;
    }

    private List<Object> getTargetInstancesByRequest(String targetName, List<Object> instances,
            Map<String, List<String>> header) {
        List<String> requestTags = routerConfig.getRequestTags();
        if (CollectionUtils.isEmpty(requestTags)) {
            return instances;
        }

        // 用于匹配实例的tags集合
        Map<String, String> tags = new HashMap<>();

        // 用于过滤实例的tags集合，value为null，代表含有该标签的实例全部过滤，不判断value值
        Map<String, String> mismatchTags = new HashMap<>();
        for (String key : header.keySet()) {
            if (!requestTags.contains(key)) {
                continue;
            }
            mismatchTags.put(key, null);
            List<String> values = header.get(key);
            if (!CollectionUtils.isEmpty(values) && StringUtils.isExist(values.get(0))) {
                tags.put(key, values.get(0));
            }
        }
        if (StringUtils.isExist(tags.get(VERSION_KEY))) {
            mismatchTags.put(VERSION_KEY, tags.get(VERSION_KEY));
        } else {
            // 所有实例都含有version，所以不能存入null值
            mismatchTags.remove(VERSION_KEY);
        }
        boolean isReturnAllInstancesWhenMismatch = false;
        if (CollectionUtils.isEmpty(mismatchTags)) {
            // 不传入header时，优先匹配无标签实例，没有无标签实例时，返回全部实例
            mismatchTags = allMismatchTags;
            isReturnAllInstancesWhenMismatch = true;
        }
        List<Object> result = RuleStrategyHandler.INSTANCE.getMatchInstancesByRequest(targetName, instances, tags);
        if (CollectionUtils.isEmpty(result)) {
            result = RuleStrategyHandler.INSTANCE.getMismatchInstances(targetName, instances,
                    Collections.singletonList(mismatchTags), isReturnAllInstancesWhenMismatch);
        }
        return result;
    }

    private List<Object> getTargetInstancesByRules(String targetName, List<Object> instances, String path,
            Map<String, List<String>> header) {
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME);
        if (RouterConfiguration.isInValid(configuration)) {
            return instances;
        }
        List<Rule> rules = FlowRuleUtils.getFlowRules(configuration, targetName, path, AppCache.INSTANCE.getAppName());
        if (CollectionUtils.isEmpty(rules)) {
            return instances;
        }
        List<Route> routes = getRoutes(rules, header);
        if (!CollectionUtils.isEmpty(routes)) {
            return RuleStrategyHandler.INSTANCE.getMatchInstances(targetName, instances, routes);
        }
        return RuleStrategyHandler.INSTANCE
                .getMismatchInstances(targetName, instances, RuleUtils.getTags(rules), true);
    }

    /**
     * 获取匹配的路由
     *
     * @param list 有效的规则
     * @param header header
     * @return 匹配的路由
     */
    private List<Route> getRoutes(List<Rule> list, Map<String, List<String>> header) {
        for (Rule rule : list) {
            List<Route> routeList = getRoutes(header, rule);
            if (!CollectionUtils.isEmpty(routeList)) {
                return routeList;
            }
        }
        return Collections.emptyList();
    }

    private List<Route> getRoutes(Map<String, List<String>> header, Rule rule) {
        Match match = rule.getMatch();
        if (match == null) {
            return rule.getRoute();
        }
        boolean isFullMatch = match.isFullMatch();
        Map<String, List<MatchRule>> headers = match.getHeaders();
        if (CollectionUtils.isEmpty(headers)) {
            return rule.getRoute();
        }
        for (Map.Entry<String, List<MatchRule>> entry : headers.entrySet()) {
            String key = entry.getKey();
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                List<String> list = header.get(key);
                String arg = list == null ? null : list.get(0);
                if (!isFullMatch && matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果不是全匹配，且匹配了一个，那么直接return
                    return rule.getRoute();
                }
                if (isFullMatch && !matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果是全匹配，且有一个不匹配，则继续下一个规则
                    return Collections.emptyList();
                }
            }
        }
        if (isFullMatch) {
            // 如果是全匹配，走到这里，说明没有不匹配的，直接return
            return rule.getRoute();
        }

        // 如果不是全匹配，走到这里，说明没有一个规则能够匹配上，则继续下一个规则
        return Collections.emptyList();
    }
}
