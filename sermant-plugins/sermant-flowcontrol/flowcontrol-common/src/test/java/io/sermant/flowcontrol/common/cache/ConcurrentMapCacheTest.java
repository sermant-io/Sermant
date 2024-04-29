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

package io.sermant.flowcontrol.common.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

/**
 * cache test
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class ConcurrentMapCacheTest {
    /**
     * cache test
     */
    @Test
    public void test() {
        String key = "test";
        String value = "val";
        final ConcurrentMapCache<String, String> cache = new ConcurrentMapCache<>();
        cache.put(key, value);
        assertEquals(cache.get(key), value);
        cache.evict(key);
        assertNull(cache.get(key));
        cache.put(key, value);
        assertEquals(1, cache.size());
        cache.release();
        assertEquals(0, cache.size());
        assertTrue(cache.getCacheTarget() instanceof ConcurrentHashMap);
    }
}
