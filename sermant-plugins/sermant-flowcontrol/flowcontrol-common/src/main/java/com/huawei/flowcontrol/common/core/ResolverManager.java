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
 * Resolver Manager
 *
 * @author zhouss
 * @since 2021-11-16
 */
public enum ResolverManager {
    /**
     * single case
     */
    INSTANCE;

    /**
     * All resolvers are loaded based on SPI
     */
    private final Map<String, AbstractResolver<?>> resolversMap = new HashMap<>();

    /**
     * parser configuration prefix collection
     */
    private Set<String> resolverConfigPrefix;

    ResolverManager() {
        loadSpiResolvers();
    }

    /**
     * Check whether the key is configured for a flow control rule
     *
     * @param key configuration key
     * @return is the configuration meets the requirements
     */
    public boolean isTarget(String key) {
        return resolverConfigPrefix.stream().anyMatch(key::startsWith);
    }

    /**
     * parsing configuration
     *
     * @param rulesMap the rule data obtained by the configuration center
     */
    public void resolve(Map<String, String> rulesMap) {
        resolve(rulesMap, false);
    }

    /**
     * parsing configuration
     *
     * @param rulesMap the rule data obtained by the configuration center
     * @param isForDelete whether to remove the scene
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
     * single kv analysis
     *
     * @param key key
     * @param value value
     * @param isForDelete whether it's a delete key
     */
    public void resolve(String key, String value, boolean isForDelete) {
        final Set<Map.Entry<String, AbstractResolver<?>>> resolvers = resolversMap.entrySet();
        for (Map.Entry<String, AbstractResolver<?>> resolverEntry : resolvers) {
            if (!key.startsWith(resolverEntry.getKey())) {
                continue;
            }
            String businessKey = key.substring(resolverEntry.getKey().length());

            // Matches the parser that starts with this configuration and updates the parser content
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
     * Transform the relevant flow control rules based on yaml converter
     *
     * @param value rule
     * @return the converted configuration
     */
    private Map<String, Object> tryResolveWithYaml(String value) {
        final Map<String, Object> kvMap = new HashMap<>();
        final Optional<Map<String, Object>> convert = OperationManager.getOperation(YamlConverter.class)
                .convert(value, Map.class);
        if (convert.isPresent()) {
            final Map<String, Object> map = convert.get();
            MapUtils.resolveNestMap(kvMap, map, null);
        }
        return kvMap;
    }

    /**
     * Check whether the corresponding service scenario rules exist
     *
     * @param businessKey service scenario name
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
     * registerListener
     *
     * @param configKey type of the listening rule
     * @param listener listener
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
     * get the resolver according to the configuration key
     *
     * @param configKey configurationKey
     * @param <R> parsingType
     * @return Resolver
     */
    public <R extends AbstractResolver<?>> R getResolver(String configKey) {
        return (R) resolversMap.get(AbstractResolver.getConfigKeyPrefix(configKey));
    }

    private void loadSpiResolvers() {
        for (AbstractResolver<?> resolver : ServiceLoader.load(AbstractResolver.class,
                ResolverManager.class.getClassLoader())) {
            final String configKeyPrefix = AbstractResolver.getConfigKeyPrefix(resolver.getConfigKey());
            if (".".equals(configKeyPrefix)) {
                // skip empty configuration
                continue;
            }
            resolversMap.put(configKeyPrefix, resolver);
        }
        resolverConfigPrefix = resolversMap.keySet();
    }
}
