/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.config.utils;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 路由工具类
 *
 * @author provenceee
 * @since 2022-07-11
 */
public class RuleUtils {
    private static final Set<String> MATCH_KEYS = new CopyOnWriteArraySet<>();

    private static final Map<String, Set<String>> SERVICE_MATCH_KEYS = new ConcurrentHashMap<>();

    private static final int ONO_HUNDRED = 100;

    private RuleUtils() {
    }

    /**
     * 获取目标规则
     *
     * @param configuration 路由配置
     * @param targetService 目标服务
     * @param path dubbo接口名/url路径
     * @param serviceName 本服务服务名
     * @return 目标规则
     */
    public static List<Rule> getRules(RouterConfiguration configuration, String targetService, String path,
        String serviceName) {
        if (RouterConfiguration.isInValid(configuration)) {
            return Collections.emptyList();
        }
        Map<String, List<Rule>> routeRule = configuration.getRouteRule();
        if (CollectionUtils.isEmpty(routeRule)) {
            return Collections.emptyList();
        }
        List<Rule> rules = routeRule.get(targetService);
        if (CollectionUtils.isEmpty(rules)) {
            return Collections.emptyList();
        }
        List<Rule> list = new ArrayList<>();
        for (Rule rule : rules) {
            if (isTargetRule(rule, path, serviceName)) {
                list.add(rule);
            }
        }
        return list;
    }

    /**
     * 获取所有标签
     *
     * @param rules 路由规则
     * @param isReplaceDash 是否需要替换破折号为点号（dubbo需要）
     * @return 标签
     */
    public static List<Map<String, String>> getTags(List<Rule> rules, boolean isReplaceDash) {
        if (CollectionUtils.isEmpty(rules)) {
            return Collections.emptyList();
        }
        List<Map<String, String>> tags = new ArrayList<>();
        for (Rule rule : rules) {
            for (Route route : rule.getRoute()) {
                tags.add(replaceDash(route.getTags(), isReplaceDash));
            }
        }
        return tags;
    }

    /**
     * 初始化需要缓存的key
     *
     * @param configuration 路由配置
     */
    public static void initMatchKeys(RouterConfiguration configuration) {
        MATCH_KEYS.clear();
        if (RouterConfiguration.isInValid(configuration)) {
            return;
        }
        Map<String, List<Rule>> routeRules = configuration.getRouteRule();
        for (List<Rule> rules : routeRules.values()) {
            addKeys(rules, MATCH_KEYS);
        }
    }

    /**
     * 更新header key
     *
     * @param serviceName 服务名
     * @param rules 路由规则
     */
    public static void updateMatchKeys(String serviceName, List<Rule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            SERVICE_MATCH_KEYS.remove(serviceName);
            return;
        }
        Set<String> keys = SERVICE_MATCH_KEYS.computeIfAbsent(serviceName, value -> new CopyOnWriteArraySet<>());
        keys.clear();
        addKeys(rules, keys);
    }

    /**
     * 获取需要缓存的key
     *
     * @return 缓存的key
     */
    public static Set<String> getMatchKeys() {
        Set<String> keys = new HashSet<>(MATCH_KEYS);
        for (Set<String> value : SERVICE_MATCH_KEYS.values()) {
            keys.addAll(value);
        }
        return Collections.unmodifiableSet(keys);
    }

    /**
     * 选取路由
     *
     * @param routes 路由规则
     * @param isReplaceDash 是否需要替换破折号为点号（dubbo需要）
     * @return 目标路由
     */
    public static RouteResult<?> getTargetTags(List<Route> routes, boolean isReplaceDash) {
        List<Map<String, String>> tags = new ArrayList<>();
        int begin = 1;
        int num = ThreadLocalRandom.current().nextInt(ONO_HUNDRED) + 1;
        for (Route route : routes) {
            Integer weight = route.getWeight();
            if (weight == null) {
                continue;
            }
            Map<String, String> currentTag = replaceDash(route.getTags(), isReplaceDash);
            if (num >= begin && num <= begin + weight - 1) {
                return new RouteResult<>(true, currentTag);
            }
            begin += weight;
            tags.add(currentTag);
        }
        return new RouteResult<>(false, tags);
    }

    /**
     * 去掉无效的规则
     *
     * @param match 匹配规则
     */
    public static void removeInvalidRules(Match match) {
        if (match == null) {
            return;
        }
        removeInvalidMatchRule(match.getArgs());
        removeInvalidMatchRule(match.getHeaders());
        removeInvalidMatchRule(match.getAttachments());
    }

    /**
     * 将headers规则写入attachments
     *
     * @param match headers匹配规则
     */
    public static void setAttachmentsByHeaders(Match match) {
        if (match == null || !CollectionUtils.isEmpty(match.getAttachments())) {
            return;
        }

        // attachments兼容headers
        match.setAttachments(match.getHeaders());
    }

    /**
     * 去掉无效的路由
     *
     * @param routeList 路由
     */
    public static void removeInvalidRoute(List<Route> routeList) {
        routeList.removeIf(RuleUtils::isInvalidRoute);
    }

    private static void removeInvalidMatchRule(Map<String, List<MatchRule>> matchRuleMap) {
        if (matchRuleMap != null) {
            matchRuleMap.entrySet().removeIf(RuleUtils::isInvalidType);
            for (List<MatchRule> matchRules : matchRuleMap.values()) {
                matchRules.removeIf(RuleUtils::isInvalidMatchRule);
            }
        }
    }

    private static boolean isTargetRule(Rule rule, String path, String serviceName) {
        if (rule == null) {
            return false;
        }
        Match match = rule.getMatch();
        if (match != null) {
            String source = match.getSource();
            if (StringUtils.isExist(source) && !source.equals(serviceName)) {
                return false;
            }
            String matchPath = match.getPath();
            if (!CollectionUtils.isEmpty(match.getAttachments()) || !CollectionUtils.isEmpty(match.getHeaders())) {
                if (StringUtils.isExist(matchPath) && !Pattern.matches(matchPath, getInterfaceName(path))) {
                    return false;
                }
            } else if (!CollectionUtils.isEmpty(match.getArgs())) {
                if (StringUtils.isBlank(matchPath) || !matchPath.equals(path)) {
                    return false;
                }
            }
        }
        return !CollectionUtils.isEmpty(rule.getRoute());
    }

    /**
     * 在attachment和header规则匹配是，删除接口中的方法名
     *
     * @param path path
     * @return 去除方法名的path
     */
    private static String getInterfaceName(String path) {
        String[] pathList = path.split(":");
        pathList[0] = delMethodName(pathList[0]);
        return String.join(":", pathList);
    }

    /**
     * 删除方法名
     *
     * @param path path
     * @return 删除方法名的path
     */
    private static String delMethodName(String path) {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(path.split("\\.")));
        list.remove(list.size() - 1);
        return String.join(".", list);
    }

    private static boolean isInvalidType(Entry<String, List<MatchRule>> entry) {
        return StringUtils.isBlank(entry.getKey()) || CollectionUtils.isEmpty(entry.getValue());
    }

    private static boolean isInvalidMatchRule(MatchRule matchRule) {
        return matchRule == null || matchRule.getValueMatch() == null
            || CollectionUtils.isEmpty(matchRule.getValueMatch().getValues())
            || matchRule.getValueMatch().getMatchStrategy() == null;
    }

    private static boolean isInvalidRoute(Route route) {
        return route == null || CollectionUtils.isEmpty(route.getTags());
    }

    private static void addKeys(List<Rule> rules, Set<String> keys) {
        for (Rule rule : rules) {
            Match match = rule.getMatch();
            if (match == null) {
                continue;
            }
            addKeys(keys, match.getHeaders());
            addKeys(keys, match.getAttachments());
        }
    }

    private static void addKeys(Set<String> keys, Map<String, List<MatchRule>> matchRule) {
        if (CollectionUtils.isEmpty(matchRule)) {
            return;
        }
        for (String key : matchRule.keySet()) {
            // 请求头在http请求中，会统一转成小写
            keys.add(key.toLowerCase(Locale.ROOT));
        }
    }

    private static Map<String, String> replaceDash(Map<String, String> tags, boolean isReplaceDash) {
        if (!isReplaceDash) {
            return tags;
        }
        Map<String, String> map = new HashMap<>();
        tags.forEach((key, value) -> {
            if (key != null && key.contains("-")) {
                // dubbo会把key中的"-"替换成"."
                map.put(key.replace("-", "."), value);
            } else {
                map.put(key, value);
            }
        });
        return map;
    }

    /**
     * 匹配结果
     *
     * @param <T> 泛型
     * @author provenceee
     * @since 2022-07-17
     */
    public static class RouteResult<T> {
        private final boolean match;

        private final T tags;

        /**
         * 构造方法
         *
         * @param match 是否匹配
         * @param tags 目标路由
         */
        public RouteResult(boolean match, T tags) {
            this.match = match;
            this.tags = tags;
        }

        public boolean isMatch() {
            return match;
        }

        public T getTags() {
            return tags;
        }
    }
}