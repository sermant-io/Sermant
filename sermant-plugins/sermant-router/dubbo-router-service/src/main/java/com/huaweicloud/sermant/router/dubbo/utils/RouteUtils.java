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

package com.huaweicloud.sermant.router.dubbo.utils;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.entity.ValueMatch;
import com.huaweicloud.sermant.router.dubbo.strategy.TypeStrategyChooser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * 路由插件工具类
 *
 * @author provenceee
 * @since 2021-06-21
 */
public class RouteUtils {
    private RouteUtils() {
    }

    /**
     * 获取匹配的路由
     *
     * @param list 有效的规则
     * @param arguments dubbo的arguments参数
     * @param attachments dubbo的attachments参数
     * @return 匹配的路由
     */
    public static List<Route> getRoutes(List<Rule> list, Object[] arguments, Map<String, Object> attachments) {
        for (Rule rule : list) {
            Match match = rule.getMatch();
            if (match == null) {
                return rule.getRoute();
            }
            List<Route> routeList;
            if (!CollectionUtils.isEmpty(match.getAttachments()) && !CollectionUtils.isEmpty(attachments)) {
                routeList = getRoutesByAttachments(attachments, rule);
            } else if (!CollectionUtils.isEmpty(match.getArgs()) && arguments != null && arguments.length > 0) {
                routeList = getRoutesByArguments(arguments, rule);
            } else {
                routeList = Collections.emptyList();
            }
            if (!CollectionUtils.isEmpty(routeList)) {
                return routeList;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 获取匹配的泳道
     *
     * @param list 有效的规则
     * @param attachments dubbo的attachments参数
     * @param arguments dubbo的arguments参数
     * @return 匹配的泳道标记
     */
    public static List<Route> getLaneRoutes(List<Rule> list, Map<String, Object> attachments, Object[] arguments) {
        for (Rule rule : list) {
            Match match = rule.getMatch();
            if (match == null) {
                return rule.getRoute();
            }
            if (isMatchByAttachments(match.getAttachments(), attachments) && isMatchByArgs(match.getArgs(),
                arguments)) {
                return rule.getRoute();
            }
        }
        return Collections.emptyList();
    }

    private static boolean isMatchByAttachments(Map<String, List<MatchRule>> matchAttachments,
        Map<String, Object> attachments) {
        if (CollectionUtils.isEmpty(matchAttachments)) {
            return true;
        }
        for (Entry<String, List<MatchRule>> entry : matchAttachments.entrySet()) {
            String key = entry.getKey();
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String arg = Optional.ofNullable(attachments.get(key)).map(String::valueOf).orElse(null);
                if (!matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 只要一个匹配不上，那就是不匹配
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isMatchByArgs(Map<String, List<MatchRule>> matchArgs, Object[] arguments) {
        if (CollectionUtils.isEmpty(matchArgs)) {
            return true;
        }
        for (Entry<String, List<MatchRule>> entry : matchArgs.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(RouterConstant.DUBBO_SOURCE_TYPE_PREFIX)) {
                continue;
            }
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String arg = TypeStrategyChooser.INSTANCE.getValue(matchRule.getType(), key, arguments).orElse(null);
                if (!matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 只要一个匹配不上，那就是不匹配
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据arguments参数获取匹配的路由
     *
     * @param arguments dubbo的arguments参数
     * @param rule 规则
     * @return 匹配的路由
     */
    private static List<Route> getRoutesByArguments(Object[] arguments, Rule rule) {
        Match match = rule.getMatch();
        boolean isFullMatch = match.isFullMatch();
        Map<String, List<MatchRule>> args = match.getArgs();
        for (Entry<String, List<MatchRule>> entry : args.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(RouterConstant.DUBBO_SOURCE_TYPE_PREFIX)) {
                continue;
            }
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String arg = TypeStrategyChooser.INSTANCE.getValue(matchRule.getType(), key, arguments).orElse(null);
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

    /**
     * 根据attachments参数获取匹配的路由
     *
     * @param attachments dubbo的attachments参数
     * @param rule 规则
     * @return 匹配的路由
     */
    private static List<Route> getRoutesByAttachments(Map<String, Object> attachments, Rule rule) {
        Match match = rule.getMatch();
        if (match == null) {
            return rule.getRoute();
        }
        boolean isFullMatch = match.isFullMatch();
        Map<String, List<MatchRule>> attachmentsRule = match.getAttachments();
        if (CollectionUtils.isEmpty(attachmentsRule)) {
            return rule.getRoute();
        }
        for (Entry<String, List<MatchRule>> entry : attachmentsRule.entrySet()) {
            String key = entry.getKey();
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String arg = Optional.ofNullable(attachments.get(key)).map(String::valueOf).orElse(null);
                if (!isFullMatch && matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果不是全匹配，且匹配了一个，直接返回
                    return rule.getRoute();
                }
                if (isFullMatch && !matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果是全匹配，且又一个不匹配，继续下一个规则
                    return Collections.emptyList();
                }
            }
        }
        if (isFullMatch) {
            // 如果是全匹配，走到这里说明全部匹配，直接返回
            return rule.getRoute();
        }

        // 如果不是全匹配，走到这里，说明没有一个规则能够匹配，继续下一个规则
        return Collections.emptyList();
    }
}