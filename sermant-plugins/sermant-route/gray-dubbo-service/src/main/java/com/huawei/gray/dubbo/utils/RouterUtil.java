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

package com.huawei.gray.dubbo.utils;

import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.strategy.TypeStrategyChooser;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Match;
import com.huawei.route.common.gray.label.entity.MatchRule;
import com.huawei.route.common.gray.label.entity.MatchStrategy;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.route.common.gray.label.entity.ValueMatch;
import com.huawei.route.common.utils.CollectionUtils;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 应用路由插件工具类
 *
 * @author provenceee
 * @since 2021-06-21
 */
public class RouterUtil {
    private RouterUtil() {
    }

    /**
     * 获取合法的目标规则
     *
     * @param grayConfiguration 标签
     * @param targetService 目标服务
     * @param interfaceName 接口
     * @return 目标规则
     */
    public static List<Rule> getValidRules(GrayConfiguration grayConfiguration, String targetService,
        String interfaceName) {
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return Collections.emptyList();
        }
        Map<String, List<Rule>> routeRule = grayConfiguration.getRouteRule();
        if (CollectionUtils.isEmpty(routeRule) || CollectionUtils.isEmpty(routeRule.get(targetService))) {
            return Collections.emptyList();
        }
        List<Rule> list = new ArrayList<>();
        for (Rule rule : routeRule.get(targetService)) {
            if (isInvalidRule(rule, interfaceName)) {
                continue;
            }

            // 去掉无效的规则
            removeInvalidRules(rule.getMatch().getArgs());

            // 去掉无效的路由
            removeInvalidRoute(rule.getRoute());
            list.add(rule);
        }
        list.sort(Comparator.comparingInt(Rule::getPrecedence));
        return list;
    }

    private static void removeInvalidRules(Map<String, List<MatchRule>> args) {
        args.entrySet().removeIf(RouterUtil::isInvalidArgs);
        for (List<MatchRule> matchRules : args.values()) {
            matchRules.removeIf(RouterUtil::isInvalidMatchRule);
        }
    }

    private static void removeInvalidRoute(List<Route> routeList) {
        routeList.removeIf(RouterUtil::isInvalidRoute);
    }

    private static boolean isInvalidRule(Rule rule, String interfaceName) {
        if (rule == null) {
            return true;
        }
        Match match = rule.getMatch();
        if (match == null) {
            return true;
        }
        String source = match.getSource();
        if (StringUtils.isNotBlank(source) && !source.equals(DubboCache.INSTANCE.getAppName())) {
            return true;
        }
        if (!interfaceName.equals(match.getPath())) {
            return true;
        }
        if (CollectionUtils.isEmpty(match.getArgs())) {
            return true;
        }
        return CollectionUtils.isEmpty(rule.getRoute());
    }

    private static boolean isInvalidArgs(Entry<String, List<MatchRule>> entry) {
        return StringUtils.isBlank(entry.getKey()) || CollectionUtils.isEmpty(entry.getValue());
    }

    private static boolean isInvalidMatchRule(MatchRule matchRule) {
        return matchRule == null || matchRule.getValueMatch() == null
            || CollectionUtils.isEmpty(matchRule.getValueMatch().getValues())
            || matchRule.getValueMatch().getMatchStrategy() == null;
    }

    private static boolean isInvalidRoute(Route route) {
        return route == null || route.getTags() == null || StringUtils.isBlank(route.getTags().getVersion());
    }

    /**
     * 获取匹配的路由
     *
     * @param list 有效的规则
     * @param arguments dubbo的参数
     * @return 匹配的路由
     */
    public static List<Route> getRoutes(List<Rule> list, Object[] arguments) {
        for (Rule rule : list) {
            List<Route> routeList = getRoutes(arguments, rule);
            if (!CollectionUtils.isEmpty(routeList)) {
                return routeList;
            }
        }
        return Collections.emptyList();
    }

    private static List<Route> getRoutes(Object[] arguments, Rule rule) {
        Match match = rule.getMatch();
        boolean isFullMatch = match.isFullMatch();
        Map<String, List<MatchRule>> args = match.getArgs();
        for (Entry<String, List<MatchRule>> entry : args.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(GrayConstant.DUBBO_SOURCE_TYPE_PREFIX)) {
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
}