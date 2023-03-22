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

package com.huaweicloud.sermant.router.dubbo;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.EntireRule;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.entity.ValueMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类描述
 *
 * @author lilai
 * @since 2023-02-27
 */
public class RuleInitializationUtils {
    public static void initFlowMatchRule() {
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("bar1"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> attachments = new HashMap<>();
        attachments.put("bar", matchRuleList);
        Match match = new Match();
        match.setAttachments(attachments);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("version", "1.0.1");
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        EntireRule entireRule = new EntireRule();
        entireRule.setRules(ruleList);
        entireRule.setKind(RouterConstant.FLOW_MATCH_KIND);
        Map<String, List<EntireRule>> map = new HashMap<>();
        map.put("foo", Collections.singletonList(entireRule));
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetRouteRule(map);
    }

    public static void initTagMatchRule() {
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("red"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> tagMatchRule = new HashMap<>();
        tagMatchRule.put("group", matchRuleList);
        Match match = new Match();
        match.setTags(tagMatchRule);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("version", "1.0.1");
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        EntireRule entireRule = new EntireRule();
        entireRule.setRules(ruleList);
        entireRule.setKind(RouterConstant.TAG_MATCH_KIND);
        Map<String, List<EntireRule>> map = new HashMap<>();
        map.put("foo", Collections.singletonList(entireRule));
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetRouteRule(map);
    }

    public static void initAllRules() {
        // 构造flow匹配路由规则
        ValueMatch valueMatch1 = new ValueMatch();
        valueMatch1.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch1.setValues(Collections.singletonList("bar1"));
        MatchRule matchRule1 = new MatchRule();
        matchRule1.setValueMatch(valueMatch1);
        List<MatchRule> matchRuleList1 = new ArrayList<>();
        matchRuleList1.add(matchRule1);
        Map<String, List<MatchRule>> attachments = new HashMap<>();
        attachments.put("bar", matchRuleList1);
        Match match1 = new Match();
        match1.setAttachments(attachments);
        Rule rule1 = new Rule();
        rule1.setPrecedence(2);
        rule1.setMatch(match1);
        Route route1 = new Route();
        route1.setWeight(100);
        Map<String, String> tags1 = new HashMap<>();
        tags1.put("version", "1.0.1");
        route1.setTags(tags1);
        List<Route> routeList1 = new ArrayList<>();
        routeList1.add(route1);
        rule1.setRoute(routeList1);
        List<Rule> ruleList1 = new ArrayList<>();
        ruleList1.add(rule1);
        EntireRule entireRule1 = new EntireRule();
        entireRule1.setRules(ruleList1);
        entireRule1.setKind(RouterConstant.FLOW_MATCH_KIND);

        // 构造tag匹配路由规则
        ValueMatch valueMatch2 = new ValueMatch();
        valueMatch2.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch2.setValues(Collections.singletonList("red"));
        MatchRule matchRule2 = new MatchRule();
        matchRule2.setValueMatch(valueMatch2);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule2);
        Map<String, List<MatchRule>> tagMatchRule = new HashMap<>();
        tagMatchRule.put("group", matchRuleList);
        Match match2 = new Match();
        match2.setTags(tagMatchRule);
        Rule rule2 = new Rule();
        rule2.setPrecedence(2);
        rule2.setMatch(match2);
        Route route2 = new Route();
        route2.setWeight(100);
        Map<String, String> tags2 = new HashMap<>();
        tags2.put("group", "red");
        route2.setTags(tags2);
        List<Route> routeList2 = new ArrayList<>();
        routeList2.add(route2);
        rule2.setRoute(routeList2);
        List<Rule> ruleList2 = new ArrayList<>();
        ruleList2.add(rule2);
        EntireRule entireRule2 = new EntireRule();
        entireRule2.setRules(ruleList2);
        entireRule2.setKind(RouterConstant.TAG_MATCH_KIND);

        Map<String, List<EntireRule>> map = new HashMap<>();
        List<EntireRule> entireRules = new ArrayList<>();
        entireRules.add(entireRule1);
        entireRules.add(entireRule2);
        map.put("foo", entireRules);
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetRouteRule(map);
    }

    public static void initConsumerTagRules() {
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("red"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> tagMatchRule = new HashMap<>();
        tagMatchRule.put("group", matchRuleList);
        Match match = new Match();
        match.setTags(tagMatchRule);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        Map<String, String> tags = new HashMap<>();
        tags.put("group", "red");
        route.setWeight(100);
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        Map<String, List<EntireRule>> map = new HashMap<>();
        EntireRule entireRule = new EntireRule();
        entireRule.setRules(ruleList);
        entireRule.setKind(RouterConstant.TAG_MATCH_KIND);
        List<EntireRule> entireRules = new ArrayList<>();
        entireRules.add(entireRule);
        map.put("foo", entireRules);

        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetRouteRule(map);
    }

    public static void initGlobalAndServiceFlowMatchRules() {
        initFlowMatchRule();
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("bar2"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> attachments = new HashMap<>();
        attachments.put("bar", matchRuleList);
        Match match = new Match();
        match.setAttachments(attachments);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("version", "1.0.1");
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        EntireRule entireRule = new EntireRule();
        entireRule.setRules(ruleList);
        entireRule.setKind(RouterConstant.FLOW_MATCH_KIND);
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetGlobalRule(Collections.singletonList(entireRule));
    }

    public static void initGlobalFlowMatchRules() {
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("bar1"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> attachments = new HashMap<>();
        attachments.put("bar", matchRuleList);
        Match match = new Match();
        match.setAttachments(attachments);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("version", "1.0.1");
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        EntireRule entireRule = new EntireRule();
        entireRule.setRules(ruleList);
        entireRule.setKind(RouterConstant.FLOW_MATCH_KIND);
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetGlobalRule(Collections.singletonList(entireRule));
    }

    public static void initGlobalAndServiceTagMatchRules() {
        initTagMatchRule();
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("green"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> tagMatchRule = new HashMap<>();
        tagMatchRule.put("group", matchRuleList);
        Match match = new Match();
        match.setTags(tagMatchRule);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("version", "1.0.1");
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        EntireRule entireRule = new EntireRule();
        entireRule.setRules(ruleList);
        entireRule.setKind(RouterConstant.TAG_MATCH_KIND);
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetGlobalRule(Collections.singletonList(entireRule));
    }

    public static void initGlobalTagMatchRules() {
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("red"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> tagMatchRule = new HashMap<>();
        tagMatchRule.put("group", matchRuleList);
        Match match = new Match();
        match.setTags(tagMatchRule);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("version", "1.0.1");
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        EntireRule entireRule = new EntireRule();
        entireRule.setRules(ruleList);
        entireRule.setKind(RouterConstant.TAG_MATCH_KIND);
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetGlobalRule(Collections.singletonList(entireRule));
    }
}
