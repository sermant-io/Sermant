/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huawei.dubbo.registry.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Collection utility class
 *
 * @author provenceee
 * @since 2021-11-03
 */
public class CollectionUtils {
    private CollectionUtils() {
    }

    /**
     * Whether it is empty or not
     *
     * @param collection collection
     * @return Whether it is empty or not
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Whether it is empty or not
     *
     * @param map map
     * @return Whether it is empty or not
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
