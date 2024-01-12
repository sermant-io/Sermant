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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

    private static final Set<String> INJECT_TAGS = new CopyOnWriteArraySet<>();

    private static final int ONO_HUNDRED = 100;

    private static final RouterConfig ROUTER_CONFIG = PluginConfigManager.getPluginConfig(RouterConfig.class);

    private RuleUtils() {
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
     * 获取具体规则
     *
     * @param configuration 路由标签缓存
     * @param targetService 目标服务
     * @param kind 规则类型
     * @return 规则列表
     */
    public static List<Rule> getRules(RouterConfiguration configuration, String targetService, String kind) {
        Map<String, Map<String, List<Rule>>> serviceRouteRule = configuration.getRouteRule();
        if (CollectionUtils.isEmpty(serviceRouteRule)) {
            return getGlobalRule(configuration, kind);
        }

        Map<String, List<Rule>> serviceRuleMap = serviceRouteRule.get(kind);
        if (CollectionUtils.isEmpty(serviceRuleMap)) {
            return getGlobalRule(configuration, kind);
        }

        List<Rule> rules = serviceRuleMap.get(targetService);
        if (CollectionUtils.isEmpty(rules)) {
            return getGlobalRule(configuration, kind);
        }
        return rules;
    }

    /**
     * 获取全局规则
     *
     * @param configuration 路由标签缓存
     * @param kind 规则类型
     * @return 规则列表
     */
    public static List<Rule> getGlobalRule(RouterConfiguration configuration, String kind) {
        Map<String, List<Rule>> globalRule = configuration.getGlobalRule();
        if (CollectionUtils.isEmpty(globalRule)) {
            return Collections.emptyList();
        }

        return globalRule.getOrDefault(kind, Collections.emptyList());
    }

    /**
     * 初始化需要缓存的key
     *
     * @param configuration 路由配置
     */
    public static void initKeys(RouterConfiguration configuration) {
        initMatchKeys(configuration);
        initInjectTags(configuration);
    }

    /**
     * 更新全局的header key
     *
     * @param configuration 路由配置
     */
    public static void initMatchKeys(RouterConfiguration configuration) {
        MATCH_KEYS.clear();
        if (!RouterConfiguration.isInValid(configuration, RouterConstant.FLOW_MATCH_KIND)) {
            Map<String, List<Rule>> routeRules = configuration.getRouteRule().get(RouterConstant.FLOW_MATCH_KIND);
            if (!CollectionUtils.isEmpty(routeRules)) {
                for (List<Rule> rules : routeRules.values()) {
                    addKeys(rules);
                }
            }
            List<Rule> globalRules = configuration.getGlobalRule().get(RouterConstant.FLOW_MATCH_KIND);
            if (!CollectionUtils.isEmpty(globalRules)) {
                addKeys(globalRules);
            }
        }
    }

    private static void initInjectTags(RouterConfiguration configuration) {
        INJECT_TAGS.clear();
        if (!RouterConfiguration.isInValid(configuration, RouterConstant.LANE_MATCH_KIND)) {
            Map<String, List<Rule>> laneRules = configuration.getRouteRule().get(RouterConstant.LANE_MATCH_KIND);
            if (!CollectionUtils.isEmpty(laneRules)) {
                for (List<Rule> rules : laneRules.values()) {
                    addTags(rules);
                }
            }
        }
    }

    /**
     * 获取需要缓存的key
     *
     * @return 缓存的key
     */
    public static Set<String> getMatchKeys() {
        return Collections.unmodifiableSet(MATCH_KEYS);
    }

    /**
     * 获取染色的key
     *
     * @return 染色的key
     */
    public static Set<String> getInjectTags() {
        return Collections.unmodifiableSet(INJECT_TAGS);
    }

    /**
     * 选取路由
     *
     * @param routes 路由规则
     * @return 目标路由
     */
    public static RouteResult<?> getTargetTags(List<Route> routes) {
        List<Map<String, String>> tags = new ArrayList<>();
        int begin = 1;
        int num = ThreadLocalRandom.current().nextInt(ONO_HUNDRED) + 1;
        for (Route route : routes) {
            Integer weight = route.getWeight();
            if (weight == null) {
                continue;
            }
            Map<String, String> currentTag = route.getTags();
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
        if (CollectionUtils.isEmpty(routes)) {
            return Collections.emptyMap();
        }
        int begin = 1;
        int num = ThreadLocalRandom.current().nextInt(ONO_HUNDRED) + 1;
        for (Route route : routes) {
            Integer weight = route.getWeight();
            if (weight == null) {
                continue;
            }
            if (num >= begin && num <= begin + weight - 1) {
                return route.getInjectTags();
            }
            begin += weight;
        }
        return Collections.emptyMap();
    }

    /**
     * 去掉无效的规则
     *
     * @param list 路由规则
     * @param isReplaceDash 是否需要把"-"替换成"."
     * @param isAppendPrefix 元数据的key值是否需要加上前缀
     */
    public static void removeInvalidRules(List<EntireRule> list, boolean isReplaceDash, boolean isAppendPrefix) {
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
            removeInvalidRules(entireRule.getKind(), entireRule.getRules(), isReplaceDash, isAppendPrefix);

            // 去掉全是无效规则的配置
            if (CollectionUtils.isEmpty(entireRule.getRules())) {
                entireIterator.remove();
            }
        }
    }

    /**
     * 去掉无效的规则
     *
     * @param kind 规则类型
     * @param rules 规则
     * @param isReplaceDash 是否需要把"-"替换成"."
     * @param isAppendPrefix 元数据的key值是否需要加上前缀
     */
    public static void removeInvalidRules(String kind, List<Rule> rules, boolean isReplaceDash,
            boolean isAppendPrefix) {
        Iterator<Rule> ruleIterator = rules.iterator();
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
            removeInvalidRoute(routes, kind, isReplaceDash, isAppendPrefix);

            List<Route> fallback = rule.getFallback();
            if (!CollectionUtils.isEmpty(fallback)) {
                // 去掉无效的fallback路由和修复同标签规则的fallback路由
                removeInvalidRoute(fallback, kind, isReplaceDash, isAppendPrefix);
            }

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
                removeInvalidTagMatch(rule.getMatch(), isAppendPrefix);
                continue;
            }

            if (RouterConstant.LANE_MATCH_KIND.equals(kind)) {
                // 去掉无效的规则
                removeInvalidLaneMatch(rule.getMatch());
            }
        }
    }

    /**
     * 获取目标规则
     *
     * @param configuration 路由配置
     * @param method 方法名
     * @param path dubbo接口名/url路径
     * @param serviceName 本服务服务名
     * @param protocol 获取哪种协议的规则
     * @return 目标规则
     */
    public static List<Rule> getLaneRules(RouterConfiguration configuration, String method, String path,
            String serviceName, Protocol protocol) {
        if (RouterConfiguration.isInValid(configuration, RouterConstant.LANE_MATCH_KIND)) {
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

    /**
     * 转化成元数据中的key
     *
     * @param key key
     * @return 元数据中的key
     */
    public static String getMetaKey(String key) {
        if (RouterConstant.VERSION.equals(key)) {
            return RouterConstant.META_VERSION_KEY;
        }
        if (RouterConstant.ZONE.equals(key)) {
            return RouterConstant.META_ZONE_KEY;
        }
        return RouterConstant.PARAMETERS_KEY_PREFIX + key;
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
     * @param isAppendPrefix 元数据的key值是否需要加上前缀
     */
    private static void removeInvalidTagMatch(Match match, boolean isAppendPrefix) {
        if (match == null) {
            return;
        }
        Map<String, List<MatchRule>> tags = match.getTags();
        removeInvalidMatchRule(tags);
        if (!CollectionUtils.isEmpty(tags) && isAppendPrefix) {
            Map<String, List<MatchRule>> newTags = new HashMap<>();
            tags.forEach((key, value) -> newTags.put(getMetaKey(key), value));
            match.setTags(newTags);
        }
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
     * @param kind 规则类型
     * @param isReplaceDash 是否替换破折号
     * @param isAppendPrefix 是否拼接前缀
     */
    private static void removeInvalidRoute(List<Route> routeList, String kind, boolean isReplaceDash,
            boolean isAppendPrefix) {
        boolean removed = routeList.removeIf(route -> isInvalidRoute(route, kind, isReplaceDash, isAppendPrefix));
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

    private static boolean isInvalidRoute(Route route, String kind, boolean isReplaceDash, boolean isAppendPrefix) {
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
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String key = entry.getKey();
            if (!RouterConstant.CONSUMER_TAG.equals(entry.getValue())) {
                continue;
            }
            if (RouterConstant.VERSION.equals(key)) {
                tags.put(key, ROUTER_CONFIG.getRouterVersion());
                route.setWeight(ONO_HUNDRED);
                continue;
            }
            if (RouterConstant.ZONE.equals(key)) {
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
        route.setTags(replaceDashAndAppendPrefix(tags, isReplaceDash, isAppendPrefix));
        return route.getWeight() == null;
    }

    private static void addKeys(List<Rule> rules) {
        for (Rule rule : rules) {
            Match match = rule.getMatch();
            if (match == null) {
                continue;
            }
            addKeys(match.getHeaders());
            addKeys(match.getAttachments());
        }
    }

    private static void addKeys(Map<String, List<MatchRule>> matchRule) {
        if (CollectionUtils.isEmpty(matchRule)) {
            return;
        }
        for (String key : matchRule.keySet()) {
            // 请求头在http请求中，会统一转成小写
            MATCH_KEYS.add(key.toLowerCase(Locale.ROOT));
        }
    }

    private static void addTags(List<Rule> rules) {
        for (Rule rule : rules) {
            List<Route> routes = rule.getRoute();
            if (CollectionUtils.isEmpty(routes)) {
                continue;
            }
            for (Route route : routes) {
                Map<String, List<String>> tags = route.getInjectTags();
                if (!CollectionUtils.isEmpty(tags)) {
                    INJECT_TAGS.addAll(tags.keySet());
                }
            }
        }
    }

    private static Map<String, String> replaceDashAndAppendPrefix(Map<String, String> tags, boolean isReplaceDash,
            boolean isAppendPrefix) {
        if (!isReplaceDash && !isAppendPrefix) {
            return tags;
        }
        Map<String, String> map = new HashMap<>();
        tags.forEach((key, value) -> {
            if (key == null) {
                return;
            }
            String newKey;
            if (isReplaceDash) {
                // dubbo会把key中的"-"替换成"."
                newKey = key.replace(RouterConstant.DASH, RouterConstant.POINT);
            } else {
                newKey = key;
            }
            if (isAppendPrefix) {
                newKey = getMetaKey(newKey);
            }
            map.put(newKey, value);
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