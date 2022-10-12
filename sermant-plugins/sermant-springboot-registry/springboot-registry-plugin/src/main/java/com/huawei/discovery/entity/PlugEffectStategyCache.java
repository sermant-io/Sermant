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

package com.huawei.discovery.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;

/**
 * 插件生效策略缓存
 *
 * @author chengyouling
 * @since 2022-10-08
 */
public enum PlugEffectStategyCache {

    /**
     * 实例
     */
    INSTANCE;

    private Map<String, String> caches = new HashMap<String, String>();

    private final YamlConverter yamlConverter = OperationManager.getOperation(YamlConverter.class);

    /**
     * 将动态配置放入缓存中
     * @param content
     */
    public void resolve(String content) {
        final Optional<Map<String, String>> dataMap = yamlConverter.convert(content, Map.class);
        if (dataMap.isPresent()) {
            caches = dataMap.get();
        }
    }

    /**
     * 获取对应key的配置
     * @param key
     * @return
     */
    public String getConfigContent(String key) {
        return caches.get(key);
    }
}
