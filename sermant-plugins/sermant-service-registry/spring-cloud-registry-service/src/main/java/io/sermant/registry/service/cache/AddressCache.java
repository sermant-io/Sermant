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

package io.sermant.registry.service.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.registry.config.GraceConfig;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Upstream address caching
 *
 * @author provenceee
 * @since 2022-05-26
 */
public enum AddressCache {
    /**
     * Singleton
     */
    INSTANCE;

    private final Cache<String, String> cache;

    AddressCache() {
        GraceConfig pluginConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
        cache = CacheBuilder.newBuilder()
                .maximumSize(pluginConfig.getUpstreamAddressMaxSize()) // 设置缓存的最大容量
                .expireAfterWrite(pluginConfig.getUpstreamAddressExpiredTime(), TimeUnit.SECONDS) // 设置缓存失效时间
                .removalListener(new CacheRemovalListener())
                .build();
    }

    /**
     * Add an upstream address
     *
     * @param address Address
     */
    public void addAddress(String address) {
        cache.put(address, "");
    }

    /**
     * Obtain the address Set
     *
     * @return Set Address Set
     */
    public Set<String> getAddressSet() {
        return cache.asMap().keySet();
    }

    /**
     * Clear all caches
     */
    public void cleanCache() {
        cache.invalidateAll();
    }

    /**
     * get cache size
     *
     * @return size
     */
    public int size() {
        cache.cleanUp();
        return (int) cache.size();
    }

    /**
     * Cache Removal Listener
     *
     * @author provenceee
     * @since 2024-09-27
     */
    private static class CacheRemovalListener implements RemovalListener<String, String> {
        private static final Logger LOGGER = LoggerFactory.getLogger();

        @Override
        public void onRemoval(RemovalNotification<String, String> notification) {
            LOGGER.log(Level.INFO, "[{0}] will remove, type is [{1}]", new Object[]{notification.getKey(),
                    notification.getCause()});
        }
    }
}