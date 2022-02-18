/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Match;
import com.huawei.route.common.gray.label.entity.MatchRule;
import com.huawei.route.common.gray.label.entity.MatchStrategy;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.route.common.gray.label.entity.ValueMatch;
import com.huawei.route.common.utils.CollectionUtils;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 应用路由插件工具类
 *
 * @author l30008180
 * @since 2021年6月21日
 */
public class RouterUtil {
    // dubbo请求中下游应用名
    private static final String REMOTE_APP_NAME = "remote.application";

    // dubbo请求中是否为consumer的key值
    private static final String CONSUMER_KEY = "side";

    // dubbo请求中是否为consumer的value值
    private static final String CONSUMER_VALUE = "consumer";

    private RouterUtil() {
    }

    /**
     * 根据Dubbo服务地址获取下游服务名：参数中的remote.application数据
     *
     * @param url dubbo请求地址
     * @return 下游服务名
     */
    public static String getTargetService(URL url) {
        return url.getParameter(REMOTE_APP_NAME);
    }

    /**
     * 根据Dubbo服务地址获取是否为消费者：参数中的side数据
     *
     * @param url dubbo请求地址
     * @return 是否为消费者
     */
    public static boolean isConsumer(URL url) {
        return CONSUMER_VALUE.equals(url.getParameter(CONSUMER_KEY));
    }

    /**
     * 根据invocation获取ldc
     *
     * @param invocation invocation
     * @return LDC
     */
    public static String getLdc(Invocation invocation) {
        String ldc = invocation.getAttachment(GrayConstant.GRAY_LDC);
        return StringUtils.isBlank(ldc) ? getLdc() : ldc;
    }

    /**
     * 获取当前服务的ldc
     *
     * @return LDC
     */
    public static String getLdc() {
        GrayConfiguration grayConfiguration = LabelCache.getLabel(DubboCache.getLabelName());
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return GrayConstant.GRAY_DEFAULT_LDC;
        }
        CurrentTag currentTag = grayConfiguration.getCurrentTag();
        if (currentTag == null || StringUtils.isBlank(currentTag.getLdc())) {
            return GrayConstant.GRAY_DEFAULT_LDC;
        }
        return currentTag.getLdc();
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
        List<Rule> list = new ArrayList<Rule>();
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
        Collections.sort(list, new Comparator<Rule>() {
            @Override
            public int compare(Rule o1, Rule o2) {
                return o1.getPrecedence() - o2.getPrecedence();
            }
        });
        return list;
    }

    private static void removeInvalidRules(Map<String, List<MatchRule>> args) {
        Iterator<Entry<String, List<MatchRule>>> matchRuleListIterator = args.entrySet().iterator();
        while (matchRuleListIterator.hasNext()) {
            if (isInvalidArgs(matchRuleListIterator.next())) {
                matchRuleListIterator.remove();
            }
        }
        for (List<MatchRule> matchRules : args.values()) {
            Iterator<MatchRule> matchRuleIterator = matchRules.iterator();
            while (matchRuleIterator.hasNext()) {
                if (isInvalidMatchRule(matchRuleIterator.next())) {
                    matchRuleIterator.remove();
                }
            }
        }
    }

    private static void removeInvalidRoute(List<Route> routeList) {
        Iterator<Route> routeIterator = routeList.iterator();
        while (routeIterator.hasNext()) {
            if (isInvalidRoute(routeIterator.next())) {
                routeIterator.remove();
            }
        }
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
        if (StringUtils.isNotBlank(source) && !source.equals(DubboCache.getAppName())) {
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
            if (routeList != null) {
                return routeList;
            }
        }
        return null;
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
                String arg = TypeStrategyChooser.INSTANCE.getValue(matchRule.getType(), key, arguments);
                if (!isFullMatch && matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果不是全匹配，且匹配了一个，那么直接return
                    return rule.getRoute();
                }
                if (isFullMatch && !matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果是全匹配，且有一个不匹配，则继续下一个规则
                    return null;
                }
            }
        }
        if (isFullMatch) {
            // 如果是全匹配，走到这里，说明没有不匹配的，直接return
            return rule.getRoute();
        }

        // 如果不是全匹配，走到这里，说明没有一个规则能够匹配上，则继续下一个规则
        return null;
    }
}