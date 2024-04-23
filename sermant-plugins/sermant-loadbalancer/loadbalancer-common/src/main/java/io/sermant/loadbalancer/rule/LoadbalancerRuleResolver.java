/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.loadbalancer.rule;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.utils.StringUtils;
import io.sermant.loadbalancer.config.DubboLoadbalancerType;
import io.sermant.loadbalancer.config.RibbonLoadbalancerType;
import io.sermant.loadbalancer.config.SpringLoadbalancerType;
import io.sermant.loadbalancer.listener.CacheListener;
import io.sermant.loadbalancer.service.RuleConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * load balancing rule resolver
 *
 * @author zhouss
 * @since 2022-08-09
 */
public class LoadbalancerRuleResolver implements RuleResolver<LoadbalancerRule> {
    /**
     * lb configuration prefix
     */
    public static final String LOAD_BALANCER_PREFIX = "servicecomb.loadbalance.";

    /**
     * traffic mark prefix
     */
    public static final String MATCH_GROUP_PREFIX = "servicecomb.matchGroup.";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String EMPTY_STR = "";

    private final RuleConverter converter;

    /**
     * store only the service name of the matchGroup
     * <pre>
     *     key: business scenario name
     *     value: matching service name
     * </pre>
     */
    private final Map<String, String> serviceCache = new HashMap<>();

    /**
     * only the loadbalancer rule type is stored
     * <pre>
     *     key: business scenario name
     *     value: load balancing type
     * </pre>
     */
    private final Map<String, String> ruleCache = new HashMap<>();

    /**
     * The service cache listener needs to refresh the cache when the configuration is updated
     */
    private final List<CacheListener> cacheListeners = new ArrayList<>();

    /**
     * ruleCache key: business scenario name value: load balancing rule
     */
    private Map<String, LoadbalancerRule> rules = new ConcurrentHashMap<>();

    /**
     * rule constructor
     */
    public LoadbalancerRuleResolver() {
        this.converter = PluginServiceManager.getPluginService(RuleConverter.class);
    }

    @Override
    public Optional<LoadbalancerRule> resolve(DynamicConfigEvent event) {
        final Optional<LoadbalancerRule> loadbalancerRule = handleConfig(event);
        loadbalancerRule.ifPresent(rule -> {
            this.notifyCache(rule, event);
            log(rule, event);
        });
        return loadbalancerRule;
    }

    private void log(LoadbalancerRule rule, DynamicConfigEvent event) {
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            LOGGER.info(String.format(Locale.ENGLISH, "[loadbalancer-plugin]Rule [%s] has been deleted!",
                    rule.toString()));
        } else if (event.getEventType() == DynamicConfigEventType.MODIFY) {
            if (rule instanceof ChangedLoadbalancerRule) {
                final LoadbalancerRule newRule = ((ChangedLoadbalancerRule) rule).getNewRule();
                final LoadbalancerRule oldRule = ((ChangedLoadbalancerRule) rule).getOldRule();
                LOGGER.info(String.format(Locale.ENGLISH, "[loadbalancer-plugin]Rule [%s] has changed to [%s]!",
                        oldRule.toString(), newRule.toString()));
            }
        } else {
            LOGGER.info(String.format(Locale.ENGLISH, "[loadbalancer-plugin]Rule [%s] has been added!",
                    rule.toString()));
        }
    }

    private Optional<LoadbalancerRule> handleConfig(DynamicConfigEvent event) {
        final String key = event.getKey();
        if (!isTargetConfig(key)) {
            return Optional.empty();
        }
        if (key.startsWith(LOAD_BALANCER_PREFIX)) {
            handleRule(event);
        } else {
            handleMatchGroup(event);
        }
        return combine();
    }

    private boolean isTargetConfig(String key) {
        return key != null && (key.startsWith(LOAD_BALANCER_PREFIX) || key.startsWith(MATCH_GROUP_PREFIX));
    }

    /**
     * Combine matchGroup serviceName and loadbalancer rule,and return the changed rule
     *
     * @return load balancing rule
     */
    private Optional<LoadbalancerRule> combine() {
        final Map<String, LoadbalancerRule> newRules = new ConcurrentHashMap<>(ruleCache.size());
        final Set<Entry<String, String>> entries = ruleCache.entrySet();
        for (Entry<String, String> entry : entries) {
            final String serviceName = serviceCache.get(entry.getKey());
            if (serviceName == null || entry.getValue() == null) {
                continue;
            }
            newRules.put(entry.getKey(), new LoadbalancerRule(EMPTY_STR.equals(serviceName) ? null : serviceName,
                    entry.getValue()));
        }
        final Map<String, LoadbalancerRule> oldRules = new HashMap<>(this.rules);
        this.rules = newRules;
        return getChangedRule(oldRules, newRules);
    }

    /**
     * get the rules for the change:
     *
     * <p>delete: returns the deleted rule</p>
     * <p>new: return the new rule</p>
     * <p>change: returns the rule before and after the change, see{@link ChangedLoadbalancerRule}</p>
     *
     * @param oldRules old rule set
     * @param newRules new rule set
     * @return modified rule
     */
    private Optional<LoadbalancerRule> getChangedRule(Map<String, LoadbalancerRule> oldRules,
            Map<String, LoadbalancerRule> newRules) {
        for (Entry<String, LoadbalancerRule> entry : oldRules.entrySet()) {
            final LoadbalancerRule rule = newRules.get(entry.getKey());
            if (rule == null) {
                // deleted rule
                return Optional.ofNullable(entry.getValue());
            }
            if (!StringUtils.equals(entry.getValue().getRule(), rule.getRule())
                    || !StringUtils.equals(entry.getValue().getServiceName(), rule.getServiceName())) {
                // the changed rules
                return Optional.of(new ChangedLoadbalancerRule(entry.getValue(), rule));
            }
        }

        // new rule
        if (newRules.size() > oldRules.size()) {
            for (Entry<String, LoadbalancerRule> entry : newRules.entrySet()) {
                if (oldRules.get(entry.getKey()) == null) {
                    return Optional.ofNullable(entry.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private void handleMatchGroup(DynamicConfigEvent event) {
        final String businessKey = event.getKey().substring(MATCH_GROUP_PREFIX.length());
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            serviceCache.remove(businessKey);
            return;
        }
        final Optional<Map> convert = converter.convert(event.getContent(), Map.class);
        if (!convert.isPresent()) {
            return;
        }
        final Map<String, Object> matcher = convert.get();
        final Optional<String> serviceNameOptional = resolveServiceName(matcher);
        serviceCache.put(businessKey, serviceNameOptional.orElse(EMPTY_STR));
    }

    private Optional<String> resolveServiceName(Map<String, Object> matcher) {
        final Object matches = matcher.get("matches");
        if (matches instanceof List) {
            final List<Map<String, Object>> list = (List<Map<String, Object>>) matches;
            if (!list.isEmpty()) {
                final Object serviceName = list.get(0).get("serviceName");
                return Optional.ofNullable(serviceName == null ? EMPTY_STR : String.valueOf(serviceName));
            }
        }
        return Optional.empty();
    }

    private void handleRule(DynamicConfigEvent event) {
        String businessKey = event.getKey().substring(LOAD_BALANCER_PREFIX.length());
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            ruleCache.remove(businessKey);
            return;
        }
        final Optional<LoadbalancerRule> ruleOptional = converter.convert(event.getContent(), LoadbalancerRule.class);
        ruleOptional.ifPresent(rule -> {
            if (isSupport(rule.getRule())) {
                ruleCache.put(businessKey, rule.getRule());
            } else {
                LOGGER.warning(String.format(Locale.ENGLISH, "Can not support loadbalancer rule: [%s]",
                        rule.getRule()));
            }
        });
    }

    private boolean isSupport(String loadbalancerType) {
        return DubboLoadbalancerType.matchLoadbalancer(loadbalancerType).isPresent()
                || SpringLoadbalancerType.matchLoadbalancer(loadbalancerType).isPresent()
                || RibbonLoadbalancerType.matchLoadbalancer(loadbalancerType).isPresent();
    }

    /**
     * add a cache listener
     *
     * @param cacheListener listener
     */
    public void addListener(CacheListener cacheListener) {
        if (cacheListener == null) {
            return;
        }
        cacheListeners.add(cacheListener);
    }

    private void notifyCache(LoadbalancerRule rule, DynamicConfigEvent event) {
        cacheListeners.forEach(cacheListener -> cacheListener.notify(rule, event));
    }

    /**
     * Check whether the current cache exists. If the configuration content exists, the cache is configured
     *
     * @return true the value is configured
     */
    public boolean isConfigured() {
        return !ruleCache.isEmpty() || !serviceCache.isEmpty();
    }

    /**
     * gets the load balancing type of the target service
     *
     * @param serviceName target service name
     * @return LoadbalancerRule
     */
    public Optional<LoadbalancerRule> getTargetServiceRule(String serviceName) {
        final Optional<LoadbalancerRule> any = rules.values().stream()
                .filter(rule -> StringUtils.equals(serviceName, rule.getServiceName()))
                .findAny();
        if (any.isPresent()) {
            return any;
        }

        // If not, check the load balancing rules that are effective for all services,
        // that is, the load balancing rules with empty serviceName.
        return rules.values().stream()
                .filter(rule -> Objects.isNull(rule.getServiceName()) && Objects.nonNull(rule.getRule()))
                .findAny();
    }
}
