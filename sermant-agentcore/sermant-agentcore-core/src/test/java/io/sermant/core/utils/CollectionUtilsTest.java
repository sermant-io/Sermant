/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.core.utils;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test for CollectionUtils
 *
 * @since 2025-07-05
 */
public class CollectionUtilsTest {

    @Test
    public void testGetFirst_whenCollectionIsEmpty() {
        List<String> emptyCollection = Collections.emptyList();
        String result = CollectionUtils.getFirst(emptyCollection, "default");
        Assertions.assertEquals("default", result);
    }

    @Test
    public void testGetFirst_whenCollectionIsNotEmpty() {
        List<String> collection = Arrays.asList("apple", "banana", "cherry");
        String result = CollectionUtils.getFirst(collection, "default");
        Assertions.assertEquals("apple", result);
    }

    @Test
    public void testGetFirst_whenCollectionIsNull() {
        List<String> nullCollection = null;
        String result = CollectionUtils.getFirst(nullCollection, "default");
        Assertions.assertEquals("default", result);
    }

    @Test
    public void testGetFirst_whenCollectionHasMultipleElements() {
        List<Integer> collection = Arrays.asList(10, 20, 30);
        Integer result = CollectionUtils.getFirst(collection, 0);
        Assertions.assertEquals(10, result);
    }
}
