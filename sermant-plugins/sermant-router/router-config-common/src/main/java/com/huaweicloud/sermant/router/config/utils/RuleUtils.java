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
import com.huaweicloud.sermant.router.config.label.entity.Match;
import com.huaweicloud.sermant.router.config.label.entity.MatchRule;
import com.huaweicloud.sermant.router.config.label.entity.Route;
import com.huaweicloud.sermant.router.config.label.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.label.entity.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    private static final Set<String> HEADER_KEYS = new CopyOnWriteArraySet<>();

    private static final Map<String, Set<String>> SERVICE_HEADER_KEYS = new ConcurrentHashMap<>();

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
     * @return 标签
     */
    public static List<Map<String, String>> getTags(List<Rule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return Collections.emptyList();
        }
        List<Map<String, String>> tags = new ArrayList<>();
        for (Rule rule : rules) {
            for (Route route : rule.getRoute()) {
                tags.add(route.getTags());
            }
        }
        return tags;
    }

    /**
     * 初始化需要缓存的key
     *
     * @param configuration 路由配置
     */
    public static void initHeaderKeys(RouterConfiguration configuration) {
        HEADER_KEYS.clear();
        if (RouterConfiguration.isInValid(configuration)) {
            return;
        }
        Map<String, List<Rule>> routeRules = configuration.getRouteRule();
        for (List<Rule> rules : routeRules.values()) {
            for (Rule rule : rules) {
                Match match = rule.getMatch();
                if (match == null) {
                    continue;
                }
                Map<String, List<MatchRule>> headers = match.getHeaders();
                if (CollectionUtils.isEmpty(headers)) {
                    continue;
                }
                HEADER_KEYS.addAll(headers.keySet());
            }
        }
    }

    /**
     * 更新header key
     *
     * @param serviceName 服务名
     * @param rules 路由规则
     */
    public static void updateHeaderKeys(String serviceName, List<Rule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            SERVICE_HEADER_KEYS.remove(serviceName);
            return;
        }
        Set<String> keys = SERVICE_HEADER_KEYS.computeIfAbsent(serviceName, value -> new CopyOnWriteArraySet<>());
        keys.clear();
        for (Rule rule : rules) {
            Match match = rule.getMatch();
            if (match == null) {
                continue;
            }
            Map<String, List<MatchRule>> headers = match.getHeaders();
            if (CollectionUtils.isEmpty(headers)) {
                continue;
            }
            keys.addAll(headers.keySet());
        }
    }

    /**
     * 获取需要缓存的key
     *
     * @return 缓存的key
     */
    public static Set<String> getHeaderKeys() {
        Set<String> keys = new HashSet<>(HEADER_KEYS);
        for (Set<String> value : SERVICE_HEADER_KEYS.values()) {
            keys.addAll(value);
        }
        return Collections.unmodifiableSet(keys);
    }

    /**
     * 选取路由
     *
     * @param routes 路由规则
     * @return 目标路由
     */
    public static RouteResult getTargetTags(List<Route> routes) {
        List<Map<String, String>> tags = new ArrayList<>();
        int begin = 1;
        int num = ThreadLocalRandom.current().nextInt(ONO_HUNDRED) + 1;
        boolean isMatch = false;
        for (Route route : routes) {
            Integer weight = route.getWeight();
            if (weight == null) {
                continue;
            }
            Map<String, String> currentTag = route.getTags();
            if (num >= begin && num <= begin + weight - 1) {
                tags.clear();
                tags.add(currentTag);
                isMatch = true;
                break;
            }
            begin += weight;
            tags.add(currentTag);
        }
        return new RouteResult(isMatch, tags);
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

    /**
     * 匹配结果
     *
     * @author provenceee
     * @since 2022-07-17
     */
    public static class RouteResult {
        private final boolean match;

        private final List<Map<String, String>> tags;

        /**
         * 构造方法
         *
         * @param match 是否匹配
         * @param tags 目标路由
         */
        public RouteResult(boolean match, List<Map<String, String>> tags) {
            this.match = match;
            this.tags = tags;
        }

        public boolean isMatch() {
            return match;
        }

        public List<Map<String, String>> getTags() {
            return tags;
        }
    }
}