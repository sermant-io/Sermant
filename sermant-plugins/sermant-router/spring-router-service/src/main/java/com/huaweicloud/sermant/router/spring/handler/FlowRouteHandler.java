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
import java.util.Optional;

/**
 * The route processor of the traffic matching method
 *
 * @author lilai
 * @since 2023-02-21
 */
public class FlowRouteHandler extends AbstractRouteHandler {
    private static final String VERSION_KEY = "version";

    private final RouterConfig routerConfig;

    // The tags set is null, which means that all instances containing the tag are filtered and the value is not judged
    private final Map<String, String> allMismatchTags;

    /**
     * Constructor
     */
    public FlowRouteHandler() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
        allMismatchTags = new HashMap<>();
        for (String requestTag : routerConfig.getRequestTags()) {
            allMismatchTags.put(requestTag, null);
        }

        // All instances contain version, so null values cannot be stored
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

        // The tags set used to match the instance
        Map<String, String> tags = new HashMap<>();

        // The tags set is null,
        // which means that all instances containing the tag are filtered and the value is not judged
        Map<String, String> mismatchTags = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : header.entrySet()) {
            String key = entry.getKey();
            if (!requestTags.contains(key)) {
                continue;
            }
            mismatchTags.put(key, null);
            List<String> values = entry.getValue();
            if (!CollectionUtils.isEmpty(values) && StringUtils.isExist(values.get(0))) {
                tags.put(key, values.get(0));
            }
        }
        if (StringUtils.isExist(tags.get(VERSION_KEY))) {
            mismatchTags.put(VERSION_KEY, tags.get(VERSION_KEY));
        } else {
            // All instances contain version, so null values cannot be stored
            mismatchTags.remove(VERSION_KEY);
        }
        boolean isReturnAllInstancesWhenMismatch = false;
        if (CollectionUtils.isEmpty(mismatchTags)) {
            // If no header is passed, the instance without a label is matched first,
            // and if there are no instances without a label, all instances are returned
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
        if (RouterConfiguration.isInValid(configuration, RouterConstant.FLOW_MATCH_KIND)) {
            return instances;
        }
        List<Rule> rules = FlowRuleUtils.getFlowRules(configuration, targetName, path, AppCache.INSTANCE.getAppName());
        if (CollectionUtils.isEmpty(rules)) {
            return instances;
        }
        Optional<Rule> ruleOptional = getRule(rules, header);
        if (ruleOptional.isPresent()) {
            return RuleStrategyHandler.INSTANCE.getFlowMatchInstances(targetName, instances, ruleOptional.get());
        }
        return RuleStrategyHandler.INSTANCE
                .getMismatchInstances(targetName, instances, RuleUtils.getTags(rules), true);
    }

    /**
     * Get a matching route
     *
     * @param list Valid rules
     * @param header header
     * @return Matching routes
     */
    private Optional<Rule> getRule(List<Rule> list, Map<String, List<String>> header) {
        for (Rule rule : list) {
            List<Route> routeList = getRoutes(header, rule);
            if (!CollectionUtils.isEmpty(routeList)) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
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
                    // If it is not all matched, and one is matched, then return directly
                    return rule.getRoute();
                }
                if (isFullMatch && !matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // If it's an all-match and there is a mismatch, move on to the next rule
                    return Collections.emptyList();
                }
            }
        }
        if (isFullMatch) {
            // If it's an all-match, go here, it means that there is no mismatch, just return
            return rule.getRoute();
        }

        // If it is not an all-match, if you go to this point, it means that none of the rules can be matched,
        // then move on to the next rule
        return Collections.emptyList();
    }
}
