/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

/**
 * periodically clear cache tests
 *
 * @author zhouss
 * @since 2022-08-29
 */
public class TimedConcurrentMapCacheTest {
    /**
     * test
     */
    @Test
    public void test() throws InterruptedException {
        int maxSize = 3;
        long evictMs = 500L;
        final Key key = new Key();
        String value = "val";
        final TimedConcurrentMapCache<Key, String> cache = new TimedConcurrentMapCache<>(maxSize, evictMs);

        // basicTest
        cache.put(key, value);
        assertEquals(cache.get(key), value);
        cache.evict(key);
        assertNull(cache.get(key));
        cache.put(key, value);
        assertEquals(1, cache.size());
        cache.release();
        assertEquals(0, cache.size());
        assertTrue(cache.getCacheTarget() instanceof ConcurrentHashMap);

        // expiredTest
        cache.put(new Key(), value);
        cache.put(new Key(), value);
        cache.put(new Key(), value);
        ReflectUtils.invokeMethod(cache, "removeEvictedCache", null, null);
        cache.put(new Key(), value);
        assertEquals(3, cache.size());
        Thread.sleep(evictMs);
        ReflectUtils.invokeMethod(cache, "removeEvictedCache", null, null);
        assertEquals(0, cache.size());
    }

    static class Key implements Timed {
        private long timestamp = System.currentTimeMillis();

        @Override
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }
}
