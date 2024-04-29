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

package io.sermant.discovery.entity;

import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.utils.StringUtils;
import io.sermant.discovery.config.PlugEffectWhiteBlackConstants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The plugin takes effect and the policy caches are cached
 *
 * @author chengyouling
 * @since 2022-10-08
 */
public enum PlugEffectStrategyCache {
    /**
     * Instance
     */
    INSTANCE;

    private final Map<String, String> caches = new ConcurrentHashMap<>();

    private final YamlConverter yamlConverter = OperationManager.getOperation(YamlConverter.class);

    /**
     * The set of service names corresponding to the current value
     */
    private Set<String> curServices = new HashSet<>();

    /**
     * Put the dynamic configuration into the cache
     *
     * @param eventType The type of event
     * @param content Event content
     */
    public void resolve(DynamicConfigEventType eventType, String content) {
        final Optional<Map<String, String>> dataMap = yamlConverter.convert(content, Map.class);
        if (eventType == DynamicConfigEventType.DELETE) {
            caches.clear();
        } else {
            if (dataMap.isPresent()) {
                Map<String, String> map = dataMap.get();
                final String strategy = map.get(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY);
                if (strategy != null && checkStrategy(map.get(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY))) {
                    caches.put(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY, strategy);
                }
                final String value = map.get(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_VALUE);
                if (value != null) {
                    caches.put(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_VALUE,
                            map.get(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_VALUE));
                }
            } else {
                caches.clear();
            }
        }
        resolveServices();
    }

    /**
     * Resolve the name of the service
     */
    private void resolveServices() {
        final String value = caches.get(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_VALUE);
        if (value == null) {
            curServices = Collections.emptySet();
            return;
        }
        final String[] parts = value.split(",");
        final HashSet<String> services = new HashSet<>();
        for (String part : parts) {
            if (part != null) {
                services.add(part.trim());
            }
        }
        curServices = services;
    }

    public Set<String> getCurServices() {
        return curServices;
    }

    private boolean checkStrategy(String strategy) {
        return StringUtils.equalsIgnoreCase(strategy, PlugEffectWhiteBlackConstants.STRATEGY_ALL)
                || StringUtils.equalsIgnoreCase(strategy, PlugEffectWhiteBlackConstants.STRATEGY_NONE)
                || StringUtils.equalsIgnoreCase(strategy, PlugEffectWhiteBlackConstants.STRATEGY_WHITE)
                || StringUtils.equalsIgnoreCase(strategy, PlugEffectWhiteBlackConstants.STRATEGY_BLACK);
    }

    /**
     * Obtain the configuration of the key
     *
     * @param key Key
     * @return Dynamically configure values
     */
    public String getConfigContent(String key) {
        return caches.get(key);
    }
}
