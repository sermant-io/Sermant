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

package com.huawei.flowcontrol.res4j.chain.context;

import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

/**
 * ChainContextTest
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class ChainContextTest {

    /**
     * test:Get the current thread variable, no more than ChainContext#MAX_SIZE,
     * IllegalArgumentException is thrown when exceeding
     */
    @Test(expected = IllegalArgumentException.class)
    public void getThreadLocalContext() {
        ChainContext.getThreadLocalContext("test");
        ChainContext.getThreadLocalContext("test1");
        ChainContext.getThreadLocalContext("test12");
        ChainContext.getThreadLocalContext("test123");
        ChainContext.getThreadLocalContext("test124");
        ChainContext.getThreadLocalContext("test125");
        ChainContext.getThreadLocalContext("test126");
        ChainContext.getThreadLocalContext("test127");
        try {
            ChainContext.getThreadLocalContext("test5");
        } finally {
            ChainContext.remove();
        }
    }

    /**
     * test to remove thread variables
     *
     * @throws NoSuchFieldException fieldless throw
     * @throws IllegalAccessException unable to get value thrown
     */
    @Test
    public void remove() throws NoSuchFieldException, IllegalAccessException {
        ChainContext.getThreadLocalContext("test");
        ChainContext.remove();
        final Field mapField = ChainContext.class.getDeclaredField("THREAD_LOCAL_CONTEXT_MAP");
        mapField.setAccessible(true);
        final Object local = mapField.get(null);
        Assert.assertTrue(local instanceof ThreadLocal);
        assertNull(((ThreadLocal<?>) local).get());

    }

    /**
     * tests to remove the specified thread variable
     *
     * @throws NoSuchFieldException fieldless throw
     * @throws IllegalAccessException unable to get value thrown
     */
    @Test
    public void testRemoveTargetName() throws NoSuchFieldException, IllegalAccessException {
        ChainContext.getThreadLocalContext("test");
        ChainContext.getThreadLocalContext("test2");
        ChainContext.remove("test2");
        final Field mapField = ChainContext.class.getDeclaredField("THREAD_LOCAL_CONTEXT_MAP");
        mapField.setAccessible(true);
        final Object local = mapField.get(null);
        Assert.assertTrue(local instanceof ThreadLocal);
        final Object map = ((ThreadLocal<?>) local).get();
        Assert.assertTrue(map instanceof Map);
        Assert.assertTrue(((Map<?, ?>) map).containsKey("test"));
        ChainContext.remove("test");
        assertNull(((ThreadLocal<?>) local).get());
    }

    /**
     * test configuration prefix
     */
    @Test
    public void testKeyPrefix() {
        String sourceName = "testKeyPrefix";
        ChainContext.setKeyPrefix(sourceName, "prefix");
        final Optional<String> keyPrefix = ChainContext.getKeyPrefix(sourceName);
        Assert.assertTrue(keyPrefix.isPresent());
        Assert.assertEquals(keyPrefix.get(), "prefix");
    }
}
