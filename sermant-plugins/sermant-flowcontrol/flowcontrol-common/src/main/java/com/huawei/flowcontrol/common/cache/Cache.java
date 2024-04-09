/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.cache;

import java.util.function.Supplier;

/**
 * cache
 *
 *
 * @author zhouss
 * @since 2022-07-21
 * @param <K> key
 * @param <V> value
 */
public interface Cache<K, V> {
    /**
     * returns the actual cached object
     *
     * @return cache object
     */
    Object getCacheTarget();

    /**
     * fetch cache
     *
     * @param key key
     * @return value
     */
    V get(K key);

    /**
     * Gets the value, and returns it using the value provided by supplier if it is empty
     *
     * @param key key
     * @param supplier value provider
     * @return 值
     */
    default V get(K key, Supplier<V> supplier) {
        final V value = get(key);
        if (value == null) {
            return supplier.get();
        }
        return value;
    }

    /**
     * setValue
     *
     * @param key key
     * @param value value
     */
    void put(K key, V value);

    /**
     * removeCacheKey
     *
     * @param key cache key to be removed
     * @return V returns the deleted value
     */
    V evict(K key);

    /**
     * release
     */
    void release();

    /**
     * cache quantity
     *
     * @return cache quantity
     */
    int size();
}
