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

package io.sermant.router.config.cache;

import io.sermant.router.config.entity.RouterConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configure caching
 *
 * @author provenceee
 * @since 2021-10-13
 */
public class ConfigCache {
    private static final Map<String, RouterConfiguration> LABEL_CACHE = new ConcurrentHashMap<>();

    private ConfigCache() {
    }

    /**
     * Obtain the specified tag
     *
     * @param cacheName The name of the cached tag
     * @return Label
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
}