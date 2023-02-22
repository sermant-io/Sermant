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

    private static final int ONO_HUNDRED = 100;

    private static final RouterConfig routerConfig = PluginConfigManager.getConfig(RouterConfig.class);

    public RuleUtils() {

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
        if (RouterConfiguration.isInValid(configuration)) {
            return;
        }
        Map<String, List<Rule>> routeRules = configuration.getRouteRule().get(RouterConstant.FLOW_MATCH_KIND);
        for (List<Rule> rules : routeRules.values()) {
            addKeys(rules, MATCH_KEYS);
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
        addKeys(rules, GLOBAL_MATCH_KEYS);
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
            return;
        }
        Set<String> keys = SERVICE_MATCH_KEYS.computeIfAbsent(serviceName, value -> new CopyOnWriteArraySet<>());
        keys.clear();
        List<Rule> rules = new ArrayList<>();
        for (EntireRule entireRule : entireRules) {
            if (RouterConstant.FLOW_MATCH_KIND.equals(entireRule.getKind())) {
                rules = entireRule.getRules();
                break;
            }
        }
        if (CollectionUtils.isEmpty(rules)) {
            SERVICE_MATCH_KEYS.remove(serviceName);
            return;
        }
        addKeys(rules, keys);
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

            // 去掉kind配置不正确的规则
            if (entireRule.getKind() == null || !RouterConstant.MATCH_KIND_LIST.contains(entireRule.getKind())) {
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
                removeInvalidRoute(routes);

                if (RouterConstant.FLOW_MATCH_KIND.equals(entireRule.getKind())) {
                    // 去掉无效的规则
                    removeInvalidMatch(rule.getMatch());

                    // 无attachments规则，将headers规则更新到attachments规则
                    setAttachmentsByHeaders(rule.getMatch());
                }

                if (RouterConstant.TAG_MATCH_KIND.equals(entireRule.getKind())) {
                    // 去掉无效的规则
                    removeInvalidTagMatch(rule.getMatch());
                }
            }
        }
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
    private static void removeInvalidRoute(List<Route> routeList) {
        boolean removed = routeList.removeIf(RuleUtils::isInvalidRoute);
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

    private static boolean isInvalidRoute(Route route) {
        if (route == null || CollectionUtils.isEmpty(route.getTags())) {
            return true;
        }

        // 修正配置为保留字段CONSUMER_TAG的规则
        Map<String, String> tags = route.getTags();
        if (route.getWeight() == null) {
            for (String key : tags.keySet()) {
                if (!RouterConstant.CONSUMER_TAG.equals(tags.get(key))) {
                    return true;
                }
                tags.put(key, routerConfig.getParameters().get(key));
            }
            route.setWeight(100);
            return false;
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