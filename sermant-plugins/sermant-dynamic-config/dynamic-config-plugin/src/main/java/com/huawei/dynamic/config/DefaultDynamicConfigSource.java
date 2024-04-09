/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config;

import com.huawei.dynamic.config.resolver.ConfigResolver;
import com.huawei.dynamic.config.resolver.DefaultConfigResolver;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * the default dynamic configuration update
 *
 * @author zhouss
 * @since 2022-04-22
 */
public class DefaultDynamicConfigSource extends DynamicConfigSource {
    private final ConfigResolver<Map<String, Object>> configResolver = new DefaultConfigResolver();

    /**
     * key: preConfigurationItem value: all key value pairs for this configuration item
     * <p>use the latest updated configuration</p>
     */
    private final Map<String, Map<String, Object>> allConfigSources = new LinkedHashMap<>();

    /**
     * The key is sorted by timestamp. The newer the key is, the sooner the date
     */
    private final PriorityQueue<TimestampKey> sortedKeys = new PriorityQueue<>();

    private Map<String, Object> configSources = new HashMap<>();

    /**
     * configuration event update
     *
     * @param event configuration event
     * @return whether to refresh the configuration
     */
    @Override
    public boolean doAccept(DynamicConfigEvent event) {
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            // Delete all key-value pairs of this configuration item
            allConfigSources.remove(event.getKey());
            sortedKeys.removeIf(timestampKey -> timestampKey.key.equals(event.getKey()));
        } else {
            final Map<String, Object> newConfigSources = configResolver.resolve(event);
            updateAllConfigSources(newConfigSources, event.getKey());
            newConfigSources.clear();
        }
        updateConfigSources();
        return true;
    }

    @Override
    public Set<String> getConfigNames() {
        return configSources.keySet();
    }

    @Override
    public Object getConfig(String key) {
        return configSources.get(key);
    }

    @Override
    public int order() {
        return 1;
    }

    private void updateConfigSources() {
        final Map<String, Object> newConfigSources = new HashMap<>(this.configSources.size());
        for (TimestampKey timestampKey : sortedKeys) {
            newConfigSources.putAll(allConfigSources.get(timestampKey.key));
        }
        this.configSources = newConfigSources;
    }

    private void updateAllConfigSources(Map<String, Object> changeConfigs, String eventKey) {
        final Map<String, Object> configMap = allConfigSources
                .getOrDefault(eventKey, new HashMap<>(changeConfigs.size()));
        configMap.putAll(changeConfigs);
        allConfigSources.put(eventKey, configMap);
        sortedKeys.removeIf(timestampKey -> timestampKey.key.equals(eventKey));
        sortedKeys.add(new TimestampKey(eventKey, System.currentTimeMillis()));
    }

    /**
     * a key with a timestamp
     *
     * @since 2022-04-18
     */
    static class TimestampKey implements Comparable<TimestampKey> {
        private final String key;

        private final long timestamp;

        TimestampKey(String key, long timestamp) {
            this.key = key;
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(TimestampKey target) {
            if (target == null) {
                return -1;
            }
            return Long.compare(this.timestamp, target.timestamp);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    @Override
    public boolean isEnabled() {
        return !configuration.isEnableCseAdapter();
    }
}
