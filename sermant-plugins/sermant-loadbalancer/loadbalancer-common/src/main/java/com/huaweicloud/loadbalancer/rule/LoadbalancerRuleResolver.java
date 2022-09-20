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

package com.huaweicloud.loadbalancer.rule;

import com.huaweicloud.loadbalancer.config.DubboLoadbalancerType;
import com.huaweicloud.loadbalancer.config.RibbonLoadbalancerType;
import com.huaweicloud.loadbalancer.config.SpringLoadbalancerType;
import com.huaweicloud.loadbalancer.listener.CacheListener;
import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.utils.StringUtils;

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
 * 负载均衡规则解析器
 *
 * @author zhouss
 * @since 2022-08-09
 */
public class LoadbalancerRuleResolver implements RuleResolver<LoadbalancerRule> {
    /**
     * lb配置前缀
     */
    public static final String LOAD_BALANCER_PREFIX = "servicecomb.loadbalance.";

    /**
     * 流量标记前缀
     */
    public static final String MATCH_GROUP_PREFIX = "servicecomb.matchGroup.";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String EMPTY_STR = "";

    private final RuleConverter converter;

    /**
     * 仅存放matchGroup的服务名
     * <pre>
     *     key: 业务场景名称
     *     value: 匹配的服务名
     * </pre>
     */
    private final Map<String, String> serviceCache = new HashMap<>();

    /**
     * 仅存放loadbalancer的规则类型
     * <pre>
     *     key: 业务场景名称
     *     value: 负载均衡类型
     * </pre>
     */
    private final Map<String, String> ruleCache = new HashMap<>();

    /**
     * 服务缓存监听器, 当配置更新时, 同时需要刷新缓存
     */
    private final List<CacheListener> cacheListeners = new ArrayList<>();

    /**
     * 规则缓存 key: 业务场景名称 value: 负载均衡规则
     */
    private Map<String, LoadbalancerRule> rules = new ConcurrentHashMap<>();

    /**
     * 规则构造器
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
     * 组合matchGroup serviceName与loadbalancer rule,并返回变更的规则
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
     * 获取变更的规则:
     *
     * <p>删除: 返回被删除的规则</p>
     * <p>新增: 返回新增的规则</p>
     * <p>更改: 返回更改前后的规则, 见{@link ChangedLoadbalancerRule}</p>
     *
     * @param oldRules 旧规则集合
     * @param newRules 新规则集合
     * @return 变更的规则
     */
    private Optional<LoadbalancerRule> getChangedRule(Map<String, LoadbalancerRule> oldRules,
            Map<String, LoadbalancerRule> newRules) {
        for (Entry<String, LoadbalancerRule> entry : oldRules.entrySet()) {
            final LoadbalancerRule rule = newRules.get(entry.getKey());
            if (rule == null) {
                // 已删除的规则
                return Optional.ofNullable(entry.getValue());
            }
            if (!StringUtils.equals(entry.getValue().getRule(), rule.getRule())
                    || !StringUtils.equals(entry.getValue().getServiceName(), rule.getServiceName())) {
                // 变更后的规则
                return Optional.of(new ChangedLoadbalancerRule(entry.getValue(), rule));
            }
        }

        // 新增规则
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
     * 添加缓存监听器
     *
     * @param cacheListener 监听器
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
     * 判断当前的缓存是否存在, 若存在配置内容则为已配置
     *
     * @return true为已配置
     */
    public boolean isConfigured() {
        return !ruleCache.isEmpty() || !serviceCache.isEmpty();
    }

    /**
     * 获取目标服务的负载均衡类型
     *
     * @param serviceName 目标服务名
     * @return LoadbalancerRule
     */
    public Optional<LoadbalancerRule> getTargetServiceRule(String serviceName) {
        final Optional<LoadbalancerRule> any = rules.values().stream()
                .filter(rule -> StringUtils.equals(serviceName, rule.getServiceName()))
                .findAny();
        if (any.isPresent()) {
            return any;
        }

        // 若没有则查看针对所有服务生效的负载均衡, 即serviceName为空的负载均衡规则
        return rules.values().stream()
                .filter(rule -> Objects.isNull(rule.getServiceName()) && Objects.nonNull(rule.getRule()))
                .findAny();
    }
}
