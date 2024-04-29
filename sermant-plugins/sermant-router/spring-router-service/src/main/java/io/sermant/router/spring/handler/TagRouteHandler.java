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

package io.sermant.router.spring.handler;

import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.config.cache.ConfigCache;
import io.sermant.router.config.entity.Match;
import io.sermant.router.config.entity.MatchRule;
import io.sermant.router.config.entity.MatchStrategy;
import io.sermant.router.config.entity.Route;
import io.sermant.router.config.entity.RouterConfiguration;
import io.sermant.router.config.entity.Rule;
import io.sermant.router.config.entity.ValueMatch;
import io.sermant.router.config.utils.TagRuleUtils;
import io.sermant.router.spring.cache.AppCache;
import io.sermant.router.spring.strategy.RuleStrategyHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The route handler of the tag matching mode
 *
 * @author lilai
 * @since 2023-02-21
 */
public class TagRouteHandler extends AbstractRouteHandler {
    @Override
    public List<Object> handle(String targetName, List<Object> instances, RequestData requestData) {
        if (!shouldHandle(instances)) {
            return instances;
        }

        List<Object> result = getTargetInstancesByRules(targetName, instances);
        return super.handle(targetName, result, requestData);
    }

    @Override
    public int getOrder() {
        return RouterConstant.TAG_HANDLER_ORDER;
    }

    private List<Object> getTargetInstancesByRules(String targetName, List<Object> instances) {
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME);
        if (RouterConfiguration.isInValid(configuration, RouterConstant.TAG_MATCH_KIND)) {
            return instances;
        }
        List<Rule> rules = TagRuleUtils.getTagRules(configuration, targetName, AppCache.INSTANCE.getAppName());
        Optional<Rule> rule = getRule(rules);
        if (rule.isPresent() && !CollectionUtils.isEmpty(rule.get().getRoute())) {
            return RuleStrategyHandler.INSTANCE.getMatchInstances(targetName, instances, rule.get());
        }
        return instances;
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
        boolean isFullMatch = match.isFullMatch();
        Map<String, List<MatchRule>> tagMatchRules = match.getTags();
        if (CollectionUtils.isEmpty(tagMatchRules)) {
            return rule.getRoute();
        }
        for (Map.Entry<String, List<MatchRule>> entry : tagMatchRules.entrySet()) {
            String key = entry.getKey();
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String tagValue = AppCache.INSTANCE.getMetadata().get(key);
                if (!isFullMatch && matchStrategy.isMatch(values, tagValue, matchRule.isCaseInsensitive())) {
                    // If it is not all matched, and one is matched, then return directly
                    return rule.getRoute();
                }
                if (isFullMatch && !matchStrategy.isMatch(values, tagValue, matchRule.isCaseInsensitive())) {
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
