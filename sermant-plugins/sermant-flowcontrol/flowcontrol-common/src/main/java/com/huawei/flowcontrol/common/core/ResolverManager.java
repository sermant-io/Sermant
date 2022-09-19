/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.core;

import com.huawei.flowcontrol.common.core.match.MatchGroupResolver;
import com.huawei.flowcontrol.common.core.resolver.AbstractResolver;
import com.huawei.flowcontrol.common.core.resolver.listener.ConfigUpdateListener;
import com.huawei.flowcontrol.common.util.StringUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.utils.MapUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * 解析器管理
 *
 * @author zhouss
 * @since 2021-11-16
 */
public enum ResolverManager {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * 基于SPI加载所有Resolver
     */
    private final Map<String, AbstractResolver<?>> resolversMap = new HashMap<>();

    /**
     * 配置解析器 当前只支持yaml格式
     */
    private final YamlConverter yamlConverter = OperationManager.getOperation(YamlConverter.class);

    /**
     * 解析器配置前缀集合
     */
    private Set<String> resolverConfigPrefix;

    ResolverManager() {
        loadSpiResolvers();
    }

    /**
     * 判断该键是否为流控规则配置
     *
     * @param key 配置键
     * @return 是否符合要求的配置
     */
    public boolean isTarget(String key) {
        return resolverConfigPrefix.stream().anyMatch(key::startsWith);
    }

    /**
     * 解析配置
     *
     * @param rulesMap 配置中心获取的规则数据
     */
    public void resolve(Map<String, String> rulesMap) {
        resolve(rulesMap, false);
    }

    /**
     * 解析配置
     *
     * @param rulesMap    配置中心获取的规则数据
     * @param isForDelete 是否是为了移除场景
     */
    public void resolve(Map<String, String> rulesMap, boolean isForDelete) {
        final Set<String> configKeyPrefixDic = resolverConfigPrefix;
        for (Map.Entry<String, String> ruleEntity : rulesMap.entrySet()) {
            final String key = ruleEntity.getKey();
            final String value = ruleEntity.getValue();
            if (isTargetConfig(key, configKeyPrefixDic)) {
                resolve(key, value, isForDelete);
            } else {
                resolve(filterValidConfig(tryResolveWithYaml(value), configKeyPrefixDic), isForDelete);
            }
        }
    }

    /**
     * 单个kv解析
     *
     * @param key         键
     * @param value       值
     * @param isForDelete 是否是删除键
     */
    public void resolve(String key, String value, boolean isForDelete) {
        final Set<Map.Entry<String, AbstractResolver<?>>> resolvers = resolversMap.entrySet();
        for (Map.Entry<String, AbstractResolver<?>> resolverEntry : resolvers) {
            if (!key.startsWith(resolverEntry.getKey())) {
                continue;
            }
            String businessKey = key.substring(resolverEntry.getKey().length());

            // 匹配以该配置打头的解析器，更新解析器内容
            final Optional<?> rule = resolverEntry.getValue().parseRule(businessKey, value, true, isForDelete);
            if (rule.isPresent() || isForDelete) {
                resolverEntry.getValue().notifyListeners(businessKey);
                LoggerFactory.getLogger().info(String.format(Locale.ENGLISH,
                    "Config [%s] has been updated or deleted successfully, raw content: [%s]", key, value));
            }
        }
    }

    private boolean isTargetConfig(String key, Set<String> configKeyPrefixDic) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        for (String prefix : configKeyPrefixDic) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> filterValidConfig(Map<String, Object> configMap, Set<String> configKeyPrefixDic) {
        final Map<String, String> result = new HashMap<>();
        for (Entry<String, Object> entry : configMap.entrySet()) {
            final Object value = entry.getValue();
            final String key = entry.getKey();
            if (!(value instanceof String) || !isTargetConfig(key, configKeyPrefixDic)) {
                continue;
            }
            result.put(key, (String) value);
        }
        return result;
    }

    /**
     * 基于yaml转换器转换相关流控规则
     *
     * @param value 规则
     * @return 转换后的配置
     */
    private Map<String, Object> tryResolveWithYaml(String value) {
        final Map<String, Object> kvMap = new HashMap<>();
        final Optional<Map<String, Object>> convert = yamlConverter.convert(value,Map.class);
        if (convert.isPresent()) {
            final Map<String, Object> map = convert.get();
            MapUtils.resolveNestMap(kvMap, map, null);
        }
        return kvMap;
    }

    /**
     * 判断是否由对应的业务场景规则
     *
     * @param businessKey 业务场景名
     * @return boolean
     */
    public boolean hasMatchedRule(String businessKey) {
        final String matchGroupKey = AbstractResolver.getConfigKeyPrefix(MatchGroupResolver.CONFIG_KEY);
        for (Map.Entry<String, AbstractResolver<?>> entry : resolversMap.entrySet()) {
            if (entry.getKey().equals(matchGroupKey)) {
                continue;
            }
            if (entry.getValue().getRules().containsKey(businessKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 注册监听器
     *
     * @param configKey 监听的规则类型
     * @param listener  监听器
     */
    public void registerListener(String configKey, ConfigUpdateListener listener) {
        String configKeyPrefix = AbstractResolver.getConfigKeyPrefix(configKey);
        final AbstractResolver<?> abstractResolver = resolversMap.get(configKeyPrefix);
        if (abstractResolver != null) {
            abstractResolver.registerListener(listener);
        }
    }

    public Map<String, AbstractResolver<?>> getResolversMap() {
        return resolversMap;
    }

    /**
     * 根据配置键获取解析器
     *
     * @param configKey 配置键
     * @param <R>       解析类型
     * @return 解析器
     */
    public <R extends AbstractResolver<?>> R getResolver(String configKey) {
        return (R) resolversMap.get(AbstractResolver.getConfigKeyPrefix(configKey));
    }

    private void loadSpiResolvers() {
        for (AbstractResolver<?> resolver : ServiceLoader.load(AbstractResolver.class,
            ResolverManager.class.getClassLoader())) {
            final String configKeyPrefix = AbstractResolver.getConfigKeyPrefix(resolver.getConfigKey());
            if (".".equals(configKeyPrefix)) {
                // 空配置跳过
                continue;
            }
            resolversMap.put(configKeyPrefix, resolver);
        }
        resolverConfigPrefix = resolversMap.keySet();
    }
}
