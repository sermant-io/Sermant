/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.config.cache;

import com.huaweicloud.sermant.router.config.entity.EnabledStrategy;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置缓存
 *
 * @author provenceee
 * @since 2021-10-13
 */
public class ConfigCache {
    private static final Map<String, RouterConfiguration> LABEL_CACHE = new ConcurrentHashMap<>();

    private static final Map<String, EnabledStrategy> STRATEGY_CACHE = new ConcurrentHashMap<>();

    private ConfigCache() {
    }

    /**
     * 获取指定标签
     *
     * @param cacheName 缓存的标签名
     * @return 标签
     */
    public static RouterConfiguration getLabel(String cacheName) {
        RouterConfiguration configuration = LABEL_CACHE.get(cacheName);
        if (configuration == null) {
            synchronized (ConfigCache.class) {
                configuration = LABEL_CACHE.get(cacheName);
                if (configuration == null) {
                    LABEL_CACHE.put(cacheName, new RouterConfiguration());
                    configuration = LABEL_CACHE.get(cacheName);
                }
            }
        }
        return configuration;
    }

    /**
     * 获取指定策略
     *
     * @param cacheName 缓存的策略名
     * @return 策略
     */
    public static EnabledStrategy getEnabledStrategy(String cacheName) {
        EnabledStrategy strategy = STRATEGY_CACHE.get(cacheName);
        if (strategy == null) {
            synchronized (ConfigCache.class) {
                strategy = STRATEGY_CACHE.get(cacheName);
                if (strategy == null) {
                    STRATEGY_CACHE.put(cacheName, new EnabledStrategy());
                    strategy = STRATEGY_CACHE.get(cacheName);
                }
            }
        }
        return strategy;
    }
}