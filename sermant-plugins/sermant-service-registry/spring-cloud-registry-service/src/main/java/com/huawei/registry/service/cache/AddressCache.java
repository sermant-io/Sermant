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

package com.huawei.registry.service.cache;

import com.huawei.registry.config.GraceConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 上游地址缓存
 *
 * @author provenceee
 * @since 2022-05-26
 */
public enum AddressCache {
    /**
     * 单例
     */
    INSTANCE;
    private final Cache<String, String> cache;

    AddressCache() {
        GraceConfig pluginConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
        cache = CacheBuilder.newBuilder()
            .maximumSize(pluginConfig.getUpstreamAddressMaxSize()) // 设置缓存的最大容量
            .expireAfterWrite(pluginConfig.getUpstreamAddressExpiredTime(), TimeUnit.SECONDS) // 设置缓存失效时间
            .build();
    }

    /**
     * 增加上游地址
     *
     * @param address 地址
     */
    public void addAddress(String address) {
        cache.put(address, "");
    }

    public Set<String> getAddressSet() {
        return cache.asMap().keySet();
    }
}