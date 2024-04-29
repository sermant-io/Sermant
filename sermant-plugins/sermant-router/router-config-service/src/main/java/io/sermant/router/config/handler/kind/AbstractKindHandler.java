/*
 *
 *  * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package io.sermant.router.config.handler.kind;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.event.RouterEventCollector;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.config.cache.ConfigCache;
import io.sermant.router.config.common.SafeConstructor;
import io.sermant.router.config.entity.EntireRule;
import io.sermant.router.config.entity.RouterConfiguration;
import io.sermant.router.config.entity.Rule;
import io.sermant.router.config.handler.AbstractHandler;
import io.sermant.router.config.utils.RuleUtils;

import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Configuration type handler (compatible with version 1.0. x)
 *
 * @author provenceee
 * @since 2024-01-11
 */
public abstract class AbstractKindHandler implements AbstractHandler {
    private static final RouterConfig ROUTER_CONFIG = PluginConfigManager.getPluginConfig(RouterConfig.class);

    /**
     * yaml
     */
    protected final Yaml yaml;

    private final String prefix;

    private final String kind;

    /**
     * Constructor
     *
     * @param prefix The prefix of the service-level key, which is used to intercept the service name
     * @param kind Configuration type
     */
    public AbstractKindHandler(String prefix, String kind) {
        this.yaml = new Yaml(new SafeConstructor(null));
        this.prefix = prefix;
        this.kind = kind;
    }

    /**
     * Route configuration type processing
     *
     * @param event Configure listening events
     * @param cacheName Cache name
     */
    @Override
    public void handle(DynamicConfigEvent event, String cacheName) {
        if (RouterConstant.GLOBAL_COMPATIBILITY_KEY_LIST.contains(event.getKey())) {
            handleGlobalRules(event, cacheName);
        } else if (RouterConstant.COMPATIBILITY_KEY_LIST.contains(event.getKey())) {
            handleRouterRules(event, cacheName);
        } else {
            handleServiceRules(event, cacheName);
        }
    }

    /**
     * Whether compatibility processing is required
     *
     * @param key Configuration key
     * @return Whether it needs to be processed
     */
    @Override
    public boolean shouldHandle(String key) {
        // return compatibilityEnabled switch value
        return ROUTER_CONFIG.isEnabledPreviousRule();
    }

    /**
     * Resolve the global configuration
     *
     * @param event event
     * @param cacheName Cache key
     */
    private void handleGlobalRules(DynamicConfigEvent event, String cacheName) {
        RouterConfiguration configuration = ConfigCache.getLabel(cacheName);
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            configuration.resetGlobalRule(getEntireRule(Collections.emptyList()));
            RuleUtils.initMatchKeys(configuration);
            RouterEventCollector.getInstance()
                    .collectGlobalRouteRuleEvent(JSON.toJSONString(configuration.getGlobalRule()));
            return;
        }

        List<Rule> list = JSONArray.parseArray(JSONObject.toJSONString(getGlobalRules(event)), Rule.class);
        RuleUtils.removeInvalidRules(kind, list, RouterConstant.DUBBO_CACHE_NAME.equals(cacheName),
                RouterConstant.DUBBO_CACHE_NAME.equals(cacheName));
        if (!CollectionUtils.isEmpty(list)) {
            list.sort((o1, o2) -> o2.getPrecedence() - o1.getPrecedence());
        }
        configuration.resetGlobalRule(getEntireRule(list));
        RuleUtils.initMatchKeys(configuration);
        RouterEventCollector.getInstance()
                .collectGlobalRouteRuleEvent(JSON.toJSONString(configuration.getGlobalRule()));
    }

    /**
     * Parsing Service-Level Configurations (Complete Set)
     *
     * @param event Event
     * @param cacheName Cache key
     */
    private void handleRouterRules(DynamicConfigEvent event, String cacheName) {
        RouterConfiguration configuration = ConfigCache.getLabel(cacheName);
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            configuration.resetRouteRule(kind, Collections.emptyMap());
            RuleUtils.initKeys(configuration);
            RouterEventCollector.getInstance()
                    .collectServiceRouteRuleEvent(JSON.toJSONString(configuration.getRouteRule()));
            return;
        }
        Map<String, String> routeRuleMap = getRouteRuleMap(event);
        Map<String, EntireRule> routeRule = new HashMap<>();
        for (Entry<String, String> entry : routeRuleMap.entrySet()) {
            List<Map<String, String>> routeRuleList = yaml.load(entry.getValue());
            if (CollectionUtils.isEmpty(routeRuleList)) {
                continue;
            }
            List<Rule> list = JSONArray.parseArray(JSONObject.toJSONString(routeRuleList), Rule.class);
            RuleUtils.removeInvalidRules(kind, list, RouterConstant.DUBBO_CACHE_NAME.equals(cacheName),
                    RouterConstant.DUBBO_CACHE_NAME.equals(cacheName));
            if (!CollectionUtils.isEmpty(list)) {
                list.sort((o1, o2) -> o2.getPrecedence() - o1.getPrecedence());
                routeRule.put(entry.getKey(), getEntireRule(list));
            }
        }
        configuration.resetRouteRule(kind, routeRule);
        RuleUtils.initKeys(configuration);
        RouterEventCollector.getInstance()
                .collectServiceRouteRuleEvent(JSON.toJSONString(configuration.getRouteRule()));
    }

    /**
     * Resolve service-level configurations
     *
     * @param event Event
     * @param cacheName Cache key
     */
    private void handleServiceRules(DynamicConfigEvent event, String cacheName) {
        RouterConfiguration configuration = ConfigCache.getLabel(cacheName);
        String serviceName = event.getKey().substring(prefix.length() + 1);
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            configuration.removeServiceRule(serviceName, kind);
            RuleUtils.initKeys(configuration);
            RouterEventCollector.getInstance()
                    .collectServiceRouteRuleEvent(JSON.toJSONString(configuration.getRouteRule()));
            return;
        }
        List<Rule> list = JSONArray.parseArray(JSONObject.toJSONString(getServiceRules(event, serviceName)),
                Rule.class);
        RuleUtils.removeInvalidRules(kind, list, RouterConstant.DUBBO_CACHE_NAME.equals(cacheName),
                RouterConstant.DUBBO_CACHE_NAME.equals(cacheName));
        if (CollectionUtils.isEmpty(list)) {
            configuration.removeServiceRule(serviceName, kind);
        } else {
            list.sort((o1, o2) -> o2.getPrecedence() - o1.getPrecedence());
            configuration.updateServiceRule(serviceName, getEntireRule(list));
        }
        RuleUtils.initKeys(configuration);
        RouterEventCollector.getInstance()
                .collectServiceRouteRuleEvent(JSON.toJSONString(configuration.getRouteRule()));
    }

    private EntireRule getEntireRule(List<Rule> rules) {
        EntireRule entireRule = new EntireRule();
        entireRule.setKind(kind);
        entireRule.setRules(rules == null ? Collections.emptyList() : rules);
        return entireRule;
    }

    private List<Map<String, Object>> getGlobalRules(DynamicConfigEvent event) {
        String content = event.getContent();
        if (StringUtils.isBlank(content)) {
            return Collections.emptyList();
        }
        Map<String, List<Map<String, Object>>> map = yaml.load(content);
        return map.get(RouterConstant.GLOBAL_ROUTER_KEY);
    }

    private List<Map<String, Object>> getServiceRules(DynamicConfigEvent event, String serviceName) {
        String content = event.getContent();
        if (StringUtils.isBlank(content)) {
            return Collections.emptyList();
        }
        Map<String, List<Map<String, Object>>> map = yaml.load(content);
        return map.get(prefix + RouterConstant.POINT + serviceName);
    }

    private Map<String, String> getRouteRuleMap(DynamicConfigEvent event) {
        String content = event.getContent();
        Map<String, String> routeRuleMap = yaml.load(content);
        if (CollectionUtils.isEmpty(routeRuleMap)) {
            return Collections.emptyMap();
        }
        return routeRuleMap;
    }
}
