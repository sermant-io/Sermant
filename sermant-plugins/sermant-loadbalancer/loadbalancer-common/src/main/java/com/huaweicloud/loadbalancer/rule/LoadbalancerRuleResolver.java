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
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 负载均衡规则解析器
 *
 * @author zhouss
 * @since 2022-08-09
 */
public class LoadbalancerRuleResolver implements RuleResolver<LoadbalancerRule> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String LOAD_BALANCER_PREFIX = "servicecomb.loadbalance";

    private static final String MATCH_GROUP_PREFIX = "servicecomb.matchGroup";

    private final RuleConverter converter;

    /**
     * 规则缓存 key: 业务场景名称 value: 负载均衡规则
     */
    private final Map<String, LoadbalancerRule> rules = new HashMap<>();

    /**
     * 服务缓存监听器, 当配置更新时, 同时需要刷新缓存
     */
    private final List<CacheListener> cacheListeners = new ArrayList<>();

    /**
     * 真实规则数量, 由于存在不匹配的matchGroup, 该数据量仅记录servicecomb.loadbalance的数量
     */
    private int realRuleSize;

    /**
     * 规则构造器
     */
    public LoadbalancerRuleResolver() {
        this.converter = PluginServiceManager.getPluginService(RuleConverter.class);
    }

    @Override
    public Optional<LoadbalancerRule> resolve(DynamicConfigEvent event) {
        final Optional<LoadbalancerRule> loadbalancerRule = handleConfig(event);
        loadbalancerRule.ifPresent(rule -> this.notifyCache(rule, event));
        return loadbalancerRule;
    }

    private Optional<LoadbalancerRule> handleConfig(DynamicConfigEvent event) {
        final String key = event.getKey();
        if (key.startsWith(LOAD_BALANCER_PREFIX)) {
            return handleRule(event);
        } else if (key.startsWith(MATCH_GROUP_PREFIX)) {
            return handleMatchGroup(event);
        } else {
            return Optional.empty();
        }
    }

    private Optional<LoadbalancerRule> handleMatchGroup(DynamicConfigEvent event) {
        final String businessKey = event.getKey().substring(MATCH_GROUP_PREFIX.length() + 1);
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            return Optional.ofNullable(rules.remove(businessKey));
        }
        final Optional<Map> convert = converter.convert(event.getContent(), Map.class);
        if (!convert.isPresent()) {
            return Optional.empty();
        }
        LoadbalancerRule rule = rules.getOrDefault(businessKey, new LoadbalancerRule());
        final Map<String, Object> matcher = convert.get();
        final Optional<String> serviceNameOptional = resolveServiceName(matcher);
        if (!serviceNameOptional.isPresent()) {
            return Optional.of(rule);
        }
        rule.setServiceName(serviceNameOptional.get());
        rules.put(businessKey, rule);
        return Optional.of(rule);
    }

    private Optional<String> resolveServiceName(Map<String, Object> matcher) {
        final Object matches = matcher.get("matches");
        if (matches instanceof List) {
            final List<Map<String, Object>> list = (List<Map<String, Object>>) matches;
            if (!list.isEmpty()) {
                return Optional.ofNullable((String) list.get(0).get("serviceName"));
            }
        }
        return Optional.empty();
    }

    private Optional<LoadbalancerRule> handleRule(DynamicConfigEvent event) {
        String businessKey = event.getKey().substring(LOAD_BALANCER_PREFIX.length() + 1);
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            final LoadbalancerRule remove = rules.remove(businessKey);
            if (remove != null) {
                realRuleSize--;
            }
            return Optional.ofNullable(remove);
        }
        final LoadbalancerRule rule = rules.getOrDefault(businessKey, new LoadbalancerRule());
        final Optional<LoadbalancerRule> ruleOptional = converter.convert(event.getContent(), LoadbalancerRule.class);
        if (ruleOptional.isPresent()) {
            final String loadbalancerType = ruleOptional.get().getRule();
            if (isSupport(loadbalancerType)) {
                rule.setRule(loadbalancerType);
                rules.put(businessKey, rule);
                realRuleSize++;
            } else {
                LOGGER.warning(String.format(Locale.ENGLISH, "Can not support loadbalancer rule [%s]",
                        loadbalancerType));
            }
        }
        return Optional.of(rule);
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
     * 宿主服务是否配置负载均衡策略
     *
     * @return true为已配置, 根据实际规则数量判断, 数量大于0则为已配置
     */
    public boolean isConfigured() {
        return realRuleSize > 0;
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
