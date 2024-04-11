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

package com.huaweicloud.sermant.core.utils;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * MapUtils
 *
 * @author zhouss
 * @since 2022-04-14
 */
public class MapUtils {
    private MapUtils() {
    }

    /**
     * Parse nested maps
     *
     * @param result result after parsing
     * @param config source map
     * @param prefix prefix of key
     */
    public static void resolveNestMap(Map<String, Object> result, Map<String, Object> config, String prefix) {
        if (config == null || config.isEmpty()) {
            return;
        }
        for (Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isBlank(prefix)) {
                key = String.format(Locale.ENGLISH, "%s.%s", prefix, key);
            }
            final Object value = entry.getValue();
            if (value instanceof Map) {
                resolveNestMap(result, (Map<String, Object>) value, key);
            } else if (value instanceof Collection) {
                result.put(key, value);
            } else {
                // Other types are reserved directly
                result.put(key, value == null ? "" : value);
            }
        }
    }

    /**
     * Determines whether Map is null or has no key-value pairs
     *
     * @param map map
     * @return result
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
