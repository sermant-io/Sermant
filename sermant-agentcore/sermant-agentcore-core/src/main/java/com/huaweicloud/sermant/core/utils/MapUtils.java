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
 * map相关解析
 *
 * @author zhouss
 * @since 2022-04-14
 */
public class MapUtils {
    private MapUtils() {
    }

    /**
     * 解析嵌套map
     *
     * @param result 解析后的最终结果
     * @param config 源配置map
     * @param prefix 键前缀
     */
    public static void resolveNestMap(Map<String, Object> result, Map<String, Object> config, String prefix) {
        if (config == null || config.isEmpty()) {
            return;
        }
        for (Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isBlank(prefix)) {
                // 键拼凑
                key = String.format(Locale.ENGLISH, "%s.%s", prefix, key);
            }
            final Object value = entry.getValue();
            if (value instanceof Map) {
                resolveNestMap(result, (Map<String, Object>) value, key);
            } else if (value instanceof Collection) {
                result.put(key, value);
            } else {
                // 其他类型均直接处理保留
                result.put(key, value == null ? "" : value);
            }
        }
    }

    /**
     * 判断Map是否为null或没有键值对
     *
     * @param map map
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
