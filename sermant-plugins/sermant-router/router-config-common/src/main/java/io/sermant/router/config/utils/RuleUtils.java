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

package io.sermant.router.config.utils;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.config.entity.EntireRule;
import io.sermant.router.config.entity.Match;
import io.sermant.router.config.entity.MatchRule;
import io.sermant.router.config.entity.Protocol;
import io.sermant.router.config.entity.Route;
import io.sermant.router.config.entity.RouterConfiguration;
import io.sermant.router.config.entity.Rule;

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
 * Routing tool class
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
     * Get all tags
     *
     * @param rules Routing rules
     * @return Label
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
     * Get specific rules
     *
     * @param configuration Route label caching
     * @param targetService Target service
     * @param kind The type of rule
     * @return List of rules
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
     * Get the global rule
     *
     * @param configuration Route label caching
     * @param kind The type of rule
     * @return List of rules
     */
    public static List<Rule> getGlobalRule(RouterConfiguration configuration, String kind) {
        Map<String, List<Rule>> globalRule = configuration.getGlobalRule();
        if (CollectionUtils.isEmpty(globalRule)) {
            return Collections.emptyList();
        }

        return globalRule.getOrDefault(kind, Collections.emptyList());
    }

    /**
     * Initialize the key that needs to be cached
     *
     * @param configuration Route configuration
     */
    public static void initKeys(RouterConfiguration configuration) {
        initMatchKeys(configuration);
        initInjectTags(configuration);
    }

    /**
     * Update the global header key
     *
     * @param configuration Route configuration
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
     * Obtain the key to be cached
     *
     * @return The cached key
     */
    public static Set<String> getMatchKeys() {
        return Collections.unmodifiableSet(MATCH_KEYS);
    }

    /**
     * Obtain the staining key
     *
     * @return The key of the staining
     */
    public static Set<String> getInjectTags() {
        return Collections.unmodifiableSet(INJECT_TAGS);
    }

    /**
     * Choose routes
     *
     * @param routes Routing rules
     * @return Destination routes
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
     * Select a swimlane marker
     *
     * @param routes Swimlane rules
     * @return Swimlane markers
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
     * Remove invalid rules
     *
     * @param list Routing rules
     * @param isReplaceDash Is it necessary to replace "-" with "."?
     * @param isAppendPrefix Whether the key value of the metadata needs to be prefixed
     */
    public static void removeInvalidRules(List<EntireRule> list, boolean isReplaceDash, boolean isAppendPrefix) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Iterator<EntireRule> entireIterator = list.iterator();
        while (entireIterator.hasNext()) {
            EntireRule entireRule = entireIterator.next();
            String kind = entireRule.getKind();

            // Remove rules that are not configured, or that are incorrectly configured
            if (CollectionUtils.isEmpty(entireRule.getRules()) || !RouterConstant.MATCH_KIND_LIST.contains(kind)) {
                entireIterator.remove();
                continue;
            }
            removeInvalidRules(entireRule.getKind(), entireRule.getRules(), isReplaceDash, isAppendPrefix);

            // Remove configurations that are all invalid rules
            if (CollectionUtils.isEmpty(entireRule.getRules())) {
                entireIterator.remove();
            }
        }
    }

    /**
     * Remove invalid rules
     *
     * @param kind The type of rule
     * @param rules rules
     * @param isReplaceDash Is it necessary to replace "-" with "."?
     * @param isAppendPrefix Whether the key value of the metadata needs to be prefixed
     */
    public static void removeInvalidRules(String kind, List<Rule> rules, boolean isReplaceDash,
            boolean isAppendPrefix) {
        Iterator<Rule> ruleIterator = rules.iterator();
        while (ruleIterator.hasNext()) {
            Rule rule = ruleIterator.next();
            List<Route> routes = rule.getRoute();

            // Remove rules that do not have routes configured
            if (CollectionUtils.isEmpty(routes)) {
                LOGGER.warning("Routes are empty, rule will be removed.");
                ruleIterator.remove();
                continue;
            }

            // Remove invalid routes and fix routes with the same label rules
            removeInvalidRoute(routes, kind, isReplaceDash, isAppendPrefix);

            List<Route> fallback = rule.getFallback();
            if (!CollectionUtils.isEmpty(fallback)) {
                // Remove invalid fallback routes and fix fallback routes with the same label rules
                removeInvalidRoute(fallback, kind, isReplaceDash, isAppendPrefix);
            }

            // Remove all invalid routes
            if (CollectionUtils.isEmpty(routes)) {
                LOGGER.warning("Routes are invalid, rule will be removed.");
                ruleIterator.remove();
                continue;
            }

            if (RouterConstant.FLOW_MATCH_KIND.equals(kind)) {
                // Remove invalid rules
                removeInvalidMatch(rule.getMatch());

                // No attachment rule, update headers rule to attachment rule
                setAttachmentsByHeaders(rule.getMatch());
                continue;
            }

            if (RouterConstant.TAG_MATCH_KIND.equals(kind)) {
                // Remove invalid rules
                removeInvalidTagMatch(rule.getMatch(), isAppendPrefix);
                continue;
            }

            if (RouterConstant.LANE_MATCH_KIND.equals(kind)) {
                // Remove invalid rules
                removeInvalidLaneMatch(rule.getMatch());
            }
        }
    }

    /**
     * Get the target rule
     *
     * @param configuration Route configuration
     * @param method Method Name
     * @param path Dubbo interface name/URL path
     * @param serviceName The name of the service
     * @param protocol Get the rules for which protocol
     * @return Target rules
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
     * Convert it into a key in the metadata
     *
     * @param key key
     * @return The key in the metadata
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
                // Http methods should not be case-sensitive
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
            // If the regular expression does not match, false will be returned
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
     * Remove invalid rules
     *
     * @param match Matching rules
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
     * Remove invalid tag matching rules
     *
     * @param match Matching rules
     * @param isAppendPrefix Whether the key value of the metadata needs to be prefixed
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
     * Remove invalid lane matching rules
     *
     * @param match Matching rules
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
     * Write header rules into attachments
     *
     * @param match Headers matching rules
     */
    private static void setAttachmentsByHeaders(Match match) {
        if (match == null || !CollectionUtils.isEmpty(match.getAttachments())) {
            return;
        }

        // attachments兼容headers
        match.setAttachments(match.getHeaders());
    }

    /**
     * Remove invalid routes and fix routes with the same label rules
     *
     * @param routeList Route list
     * @param kind The type of rule
     * @param isReplaceDash Whether to replace the dash
     * @param isAppendPrefix Whether to splice the prefix
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

        // Fix the tag rule configured to reserve the field CONSUMER_TAG and set its weight to 100
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
            // The request header is changed to lowercase in the HTTP request
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
                // dubbo will replace the "-" in the key with "."
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
     * Match results
     *
     * @param <T> Generics
     * @author provenceee
     * @since 2022-07-17
     */
    public static class RouteResult<T> {
        private final boolean match;

        private final T tags;

        /**
         * Constructor
         *
         * @param match Whether it matches or not
         * @param tags Destination routing
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