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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.EntireRule;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.Protocol;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 路由工具类
 *
 * @author provenceee
 * @since 2022-07-11
 */
public class RuleUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Set<String> MATCH_KEYS = new CopyOnWriteArraySet<>();

    private static final Set<String> GLOBAL_MATCH_KEYS = new CopyOnWriteArraySet<>();

    private static final Map<String, Set<String>> SERVICE_MATCH_KEYS = new ConcurrentHashMap<>();

    private static final Set<String> INJECT_TAGS = new CopyOnWriteArraySet<>();

    private static final Map<String, Set<String>> SERVICE_INJECT_TAGS = new ConcurrentHashMap<>();

    private static final int ONO_HUNDRED = 100;

    private static final String VERSION = "version";

    private static final String ZONE = "zone";

    private static final RouterConfig ROUTER_CONFIG = PluginConfigManager.getConfig(RouterConfig.class);

    private RuleUtils() {
    }

    /**
     * 获取所有标签
     *
     * @param rules         路由规则
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
        INJECT_TAGS.clear();
        if (RouterConfiguration.isInValid(configuration)) {
            return;
        }
        Map<String, List<Rule>> routeRules = configuration.getRouteRule().get(RouterConstant.FLOW_MATCH_KIND);
        if (!CollectionUtils.isEmpty(routeRules)) {
            for (List<Rule> rules : routeRules.values()) {
                addKeys(rules, MATCH_KEYS);
            }
        }
        Map<String, List<Rule>> laneRules = configuration.getRouteRule().get(RouterConstant.LANE_MATCH_KIND);
        if (!CollectionUtils.isEmpty(laneRules)) {
            for (List<Rule> rules : routeRules.values()) {
                addTags(rules, INJECT_TAGS);
            }
        }
    }

    /**
     * 更新全局的header key
     *
     * @param configuration 路由配置
     */
    public static void initGlobalKeys(RouterConfiguration configuration) {
        GLOBAL_MATCH_KEYS.clear();
        if (RouterConfiguration.isInValid(configuration)) {
            return;
        }
        List<Rule> rules = configuration.getGlobalRule().get(RouterConstant.FLOW_MATCH_KIND);
        if (!CollectionUtils.isEmpty(rules)) {
            addKeys(rules, GLOBAL_MATCH_KEYS);
        }
    }

    /**
     * 更新header key
     *
     * @param serviceName 服务名
     * @param entireRules 整体路由规则
     */
    public static void updateMatchKeys(String serviceName, List<EntireRule> entireRules) {
        if (CollectionUtils.isEmpty(entireRules)) {
            SERVICE_MATCH_KEYS.remove(serviceName);
            SERVICE_INJECT_TAGS.remove(serviceName);
            return;
        }
        List<Rule> flowRules = new ArrayList<>();
        List<Rule> laneRules = new ArrayList<>();
        boolean initFlowRules = false;
        boolean initLaneRules = false;
        for (EntireRule entireRule : entireRules) {
            if (RouterConstant.FLOW_MATCH_KIND.equals(entireRule.getKind()) && !initFlowRules) {
                flowRules = entireRule.getRules();
                initFlowRules = true;
                continue;
            }
            if (RouterConstant.LANE_MATCH_KIND.equals(entireRule.getKind()) && !initLaneRules) {
                laneRules.addAll(entireRule.getRules());
                initLaneRules = true;
            }
        }
        if (CollectionUtils.isEmpty(flowRules)) {
            SERVICE_MATCH_KEYS.remove(serviceName);
        } else {
            Set<String> keys = SERVICE_MATCH_KEYS.computeIfAbsent(serviceName, value -> new CopyOnWriteArraySet<>());
            keys.clear();
            addKeys(flowRules, keys);
        }
        if (CollectionUtils.isEmpty(laneRules)) {
            SERVICE_INJECT_TAGS.remove(serviceName);
        } else {
            Set<String> tags = SERVICE_INJECT_TAGS.computeIfAbsent(serviceName, value -> new CopyOnWriteArraySet<>());
            tags.clear();
            addTags(laneRules, tags);
        }
    }

    /**
     * 获取需要缓存的key
     *
     * @return 缓存的key
     */
    public static Set<String> getMatchKeys() {
        Set<String> keys = new HashSet<>(MATCH_KEYS);
        keys.addAll(GLOBAL_MATCH_KEYS);
        for (Set<String> value : SERVICE_MATCH_KEYS.values()) {
            keys.addAll(value);
        }
        return Collections.unmodifiableSet(keys);
    }

    /**
     * 获取染色的key
     *
     * @return 染色的key
     */
    public static Set<String> getMatchTags() {
        Set<String> keys = new HashSet<>(INJECT_TAGS);
        for (Set<String> value : SERVICE_INJECT_TAGS.values()) {
            keys.addAll(value);
        }
        return Collections.unmodifiableSet(keys);
    }

    /**
     * 选取路由
     *
     * @param routes        路由规则
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
     * 选取泳道标记
     *
     * @param routes 泳道规则
     * @return 泳道标记
     */
    public static Map<String, List<String>> getTargetLaneTags(List<Route> routes) {
        int begin = 1;
        int num = ThreadLocalRandom.current().nextInt(ONO_HUNDRED) + 1;
        for (Route route : routes) {
            Integer weight = route.getWeight();
            if (weight == null) {
                continue;
            }
            if (num >= begin && num <= begin + weight - 1) {
                return packageTags(route.getInjectTags());
            }
            begin += weight;
        }
        return Collections.emptyMap();
    }

    private static Map<String, List<String>> packageTags(Map<String, String> tags) {
        Map<String, List<String>> map = new HashMap<>();
        tags.forEach((key, value) -> map.put(key, Collections.singletonList(value)));
        return map;
    }

    /**
     * 去掉无效的规则
     *
     * @param list 路由规则
     */
    public static void removeInvalidRules(List<EntireRule> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Iterator<EntireRule> entireIterator = list.iterator();
        while (entireIterator.hasNext()) {
            EntireRule entireRule = entireIterator.next();
            String kind = entireRule.getKind();

            // 去掉没配置规则，或kind配置不正确的规则
            if (CollectionUtils.isEmpty(entireRule.getRules()) || !RouterConstant.MATCH_KIND_LIST.contains(kind)) {
                entireIterator.remove();
                continue;
            }

            Iterator<Rule> ruleIterator = entireRule.getRules().iterator();
            while (ruleIterator.hasNext()) {
                Rule rule = ruleIterator.next();
                List<Route> routes = rule.getRoute();

                // 去掉没有配置路由的规则
                if (CollectionUtils.isEmpty(routes)) {
                    LOGGER.warning("Routes are empty, rule will be removed.");
                    ruleIterator.remove();
                    continue;
                }

                // 去掉无效的路由和修复同标签规则的路由
                removeInvalidRoute(routes, kind);

                // 去掉全是无效路由的规则
                if (CollectionUtils.isEmpty(routes)) {
                    LOGGER.warning("Routes are invalid, rule will be removed.");
                    ruleIterator.remove();
                    continue;
                }

                if (RouterConstant.FLOW_MATCH_KIND.equals(kind)) {
                    // 去掉无效的规则
                    removeInvalidMatch(rule.getMatch());

                    // 无attachments规则，将headers规则更新到attachments规则
                    setAttachmentsByHeaders(rule.getMatch());
                    continue;
                }

                if (RouterConstant.TAG_MATCH_KIND.equals(kind)) {
                    // 去掉无效的规则
                    removeInvalidTagMatch(rule.getMatch());
                    continue;
                }

                if (RouterConstant.LANE_MATCH_KIND.equals(kind)) {
                    // 去掉无效的规则
                    removeInvalidLaneMatch(rule.getMatch());
                }
            }

            // 去掉全是无效规则的配置
            if (CollectionUtils.isEmpty(entireRule.getRules())) {
                entireIterator.remove();
            }
        }
    }

    /**
     * 获取目标规则
     *
     * @param configuration 路由配置
     * @param method        方法名
     * @param path          dubbo接口名/url路径
     * @param serviceName   本服务服务名
     * @param protocol      获取哪种协议的规则
     * @return 目标规则
     */
    public static List<Rule> getLaneRules(RouterConfiguration configuration, String method, String path,
                                          String serviceName, Protocol protocol) {
        if (RouterConfiguration.isInValid(configuration)) {
            return Collections.emptyList();
        }
        Map<String, List<Rule>> routeRule = configuration.getRouteRule().get(RouterConstant.LANE_MATCH_KIND);
        if (CollectionUtils.isEmpty(routeRule)) {
            return Collections.emptyList();
        }
        List<Rule> rules = routeRule.get(serviceName);
        if (CollectionUtils.isEmpty(rules)) {
            return Collections.emptyList();
        }
        List<Rule> list = new ArrayList<>();
        for (Rule rule : rules) {
            if (isTargetLaneRule(rule, method, path, protocol)) {
                list.add(rule);
            }
        }
        return list;
    }

    private static boolean isTargetLaneRule(Rule rule, String method, String path, Protocol protocol) {
        if (rule == null) {
            return false;
        }
        Match match = rule.getMatch();
        if (match != null) {
            if (protocol != match.getProtocol()) {
                return false;
            }
            String matchPath = match.getPath();
            if (StringUtils.isExist(matchPath)) {
                if (!matchPath.equals(path) && !isPatternMatches(matchPath, path)) {
                    return false;
                }
            }
            String matchMethod = match.getMethod();
            if (StringUtils.isExist(matchMethod)) {
                // http方法不应区分大小写
                if (!equals(matchMethod, method, protocol == Protocol.HTTP)) {
                    return false;
                }
            }
        }
        return !CollectionUtils.isEmpty(rule.getRoute());
    }

    private static boolean isPatternMatches(String regex, String path) {
        try {
            return Pattern.matches(regex, path);
        } catch (PatternSyntaxException ignored) {
            // 正则表达式不符合，返回false
            return false;
        }
    }

    private static boolean equals(String str1, String str2, boolean ignoreCase) {
        if (ignoreCase) {
            return str1.equalsIgnoreCase(str2);
        }
        return str1.equals(str2);
    }

    /**
     * 去掉无效的规则
     *
     * @param match 匹配规则
     */
    private static void removeInvalidMatch(Match match) {
        if (match == null) {
            return;
        }
        removeInvalidMatchRule(match.getArgs());
        removeInvalidMatchRule(match.getHeaders());
        removeInvalidMatchRule(match.getAttachments());
    }

    /**
     * 去掉无效的tag匹配规则
     *
     * @param match 匹配规则
     */
    private static void removeInvalidTagMatch(Match match) {
        if (match == null) {
            return;
        }
        removeInvalidMatchRule(match.getTags());
    }

    /**
     * 去掉无效的lane匹配规则
     *
     * @param match 匹配规则
     */
    private static void removeInvalidLaneMatch(Match match) {
        if (match == null) {
            return;
        }
        removeInvalidMatchRule(match.getHeaders());
        removeInvalidMatchRule(match.getParameters());
        removeInvalidMatchRule(match.getAttachments());
        removeInvalidMatchRule(match.getArgs());
    }

    /**
     * 将headers规则写入attachments
     *
     * @param match headers匹配规则
     */
    private static void setAttachmentsByHeaders(Match match) {
        if (match == null || !CollectionUtils.isEmpty(match.getAttachments())) {
            return;
        }

        // attachments兼容headers
        match.setAttachments(match.getHeaders());
    }

    /**
     * 去掉无效的路由和修复同标签规则的路由
     *
     * @param routeList 路由
     */
    private static void removeInvalidRoute(List<Route> routeList, String kind) {
        boolean removed = routeList.removeIf(route -> isInvalidRoute(route, kind));
        if (removed) {
            LOGGER.warning("Some invalid routes had been removed, please check your router configuration.");
        }
    }

    private static void removeInvalidMatchRule(Map<String, List<MatchRule>> matchRuleMap) {
        if (matchRuleMap != null) {
            matchRuleMap.entrySet().removeIf(RuleUtils::isInvalidType);
            for (List<MatchRule> matchRules : matchRuleMap.values()) {
                matchRules.removeIf(RuleUtils::isInvalidMatchRule);
            }
        }
    }

    private static boolean isInvalidType(Entry<String, List<MatchRule>> entry) {
        return StringUtils.isBlank(entry.getKey()) || CollectionUtils.isEmpty(entry.getValue());
    }

    private static boolean isInvalidMatchRule(MatchRule matchRule) {
        return matchRule == null || matchRule.getValueMatch() == null
            || CollectionUtils.isEmpty(matchRule.getValueMatch().getValues())
            || matchRule.getValueMatch().getMatchStrategy() == null;
    }

    private static boolean isInvalidRoute(Route route, String kind) {
        if (route == null) {
            return true;
        }
        if (RouterConstant.LANE_MATCH_KIND.equals(kind)) {
            return route.getWeight() == null || CollectionUtils.isEmpty(route.getInjectTags());
        }

        // 修正tag配置为保留字段CONSUMER_TAG的规则, 并将其权重设置为100
        Map<String, String> tags = route.getTags();
        if (CollectionUtils.isEmpty(tags)) {
            return true;
        }
        for (String key : tags.keySet()) {
            if (!RouterConstant.CONSUMER_TAG.equals(tags.get(key))) {
                continue;
            }
            if (VERSION.equals(key)) {
                tags.put(key, ROUTER_CONFIG.getRouterVersion());
                route.setWeight(ONO_HUNDRED);
                continue;
            }
            if (ZONE.equals(key)) {
                tags.put(key, ROUTER_CONFIG.getZone());
                route.setWeight(ONO_HUNDRED);
                continue;
            }
            Map<String, String> parameters = ROUTER_CONFIG.getParameters();
            if (!CollectionUtils.isEmpty(parameters)) {
                tags.put(key, parameters.get(key));
                route.setWeight(ONO_HUNDRED);
            }
        }
        if (route.getWeight() == null) {
            return true;
        }
        return false;
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

    private static void addTags(List<Rule> rules, Set<String> keys) {
        for (Rule rule : rules) {
            List<Route> routes = rule.getRoute();
            if (CollectionUtils.isEmpty(routes)) {
                continue;
            }
            for (Route route : routes) {
                Map<String, String> tags = route.getInjectTags();
                if (!CollectionUtils.isEmpty(tags)) {
                    keys.addAll(tags.keySet());
                }
            }
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
         * @param tags  目标路由
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