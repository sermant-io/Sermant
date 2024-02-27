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

package com.huaweicloud.sermant.router.dubbo.handler;

import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.entity.ValueMatch;
import com.huaweicloud.sermant.router.config.utils.TagRuleUtils;
import com.huaweicloud.sermant.router.dubbo.strategy.RuleStrategyHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * tag匹配方式的路由处理器
 *
 * @author lilai
 * @since 2023-02-24
 */
public class TagRouteHandler extends AbstractRouteHandler {
    @Override
    public Object handle(String targetService, List<Object> invokers, Object invocation, Map<String, String> queryMap,
            String serviceInterface) {
        if (!shouldHandle(invokers)) {
            return invokers;
        }

        List<Object> result = getTargetInvokersByRules(invokers, targetService);
        return super.handle(targetService, result, invocation, queryMap, serviceInterface);
    }

    @Override
    public int getOrder() {
        return RouterConstant.TAG_HANDLER_ORDER;
    }

    private List<Object> getTargetInvokersByRules(List<Object> invokers, String targetService) {
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        if (RouterConfiguration.isInValid(configuration, RouterConstant.TAG_MATCH_KIND)) {
            return invokers;
        }
        List<Rule> rules = TagRuleUtils.getTagRules(configuration, targetService, DubboCache.INSTANCE.getAppName());
        Optional<Rule> rule = getRule(rules);
        if (rule.isPresent() && !CollectionUtils.isEmpty(rule.get().getRoute())) {
            return RuleStrategyHandler.INSTANCE.getMatchInvokers(targetService, invokers, rule.get());
        }
        return invokers;
    }

    private Optional<Rule> getRule(List<Rule> list) {
        for (Rule rule : list) {
            List<Route> routeList = getRoutes(rule);
            if (!CollectionUtils.isEmpty(routeList)) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    private List<Route> getRoutes(Rule rule) {
        Match match = rule.getMatch();
        if (match == null) {
            return rule.getRoute();
        }
        Map<String, List<MatchRule>> tagMatchRules = match.getTags();
        if (CollectionUtils.isEmpty(tagMatchRules)) {
            return rule.getRoute();
        }
        Map<String, String> parameters = DubboCache.INSTANCE.getParameters();
        if (CollectionUtils.isEmpty(parameters)) {
            return Collections.emptyList();
        }
        boolean isFullMatch = match.isFullMatch();
        for (Map.Entry<String, List<MatchRule>> entry : tagMatchRules.entrySet()) {
            String key = entry.getKey();
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String tagValue = parameters.get(key);
                if (!isFullMatch && matchStrategy.isMatch(values, tagValue, matchRule.isCaseInsensitive())) {
                    // 如果不是全匹配，且匹配了一个，那么直接return
                    return rule.getRoute();
                }
                if (isFullMatch && !matchStrategy.isMatch(values, tagValue, matchRule.isCaseInsensitive())) {
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
