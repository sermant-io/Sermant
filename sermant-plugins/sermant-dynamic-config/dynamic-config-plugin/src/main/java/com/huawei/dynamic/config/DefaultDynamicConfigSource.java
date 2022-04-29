/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
 * 默认动态配置更新
 *
 * @author zhouss
 * @since 2022-04-22
 */
public class DefaultDynamicConfigSource extends DynamicConfigSource {
    private final ConfigResolver<Map<String, Object>> configResolver = new DefaultConfigResolver();

    /**
     * key: 前置配置项 value: 针对该配置项的所有键值对
     * <p>使用最新更新配置</p>
     */
    private final Map<String, Map<String, Object>> allConfigSources = new LinkedHashMap<>();

    /**
     * 根据时间戳排序的key, 越新则排在约前面
     */
    private final PriorityQueue<TimestampKey> sortedKeys = new PriorityQueue<>();

    private Map<String, Object> configSources = new HashMap<>();

    /**
     * 配置事件更新
     *
     * @param event 配置事件
     * @return 是否要求刷新配置
     */
    @Override
    public boolean doAccept(DynamicConfigEvent event) {
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            // 直接移除该配置项的所有键值对
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
     * 带时间戳的key
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
    }

    @Override
    public boolean isEnabled() {
        return !configuration.isEnableCseAdapter();
    }
}
