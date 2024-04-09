/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.strategy.type;

import com.huaweicloud.sermant.router.dubbo.strategy.TypeStrategy;

import org.junit.Assert;
import org.junit.Test;

/**
 * Array matching strategy testing
 *
 * @author provenceee
 * @since 2021-12-01
 */
public class ArrayTypeStrategyTest {
    /**
     * test the array strategy
     */
    @Test
    public void testValue() {
        TypeStrategy strategy = new ArrayTypeStrategy();
        String[] arr = new String[]{"foo", null};

        // normal
        Assert.assertEquals("foo", strategy.getValue(arr, "[0]").orElse(null));

        // test null
        Assert.assertNotEquals("foo", strategy.getValue(arr, "[1]").orElse(null));

        // Testing out of bounds
        Assert.assertNotEquals("foo", strategy.getValue(arr, "[2]").orElse(null));

        // Test non array
        Assert.assertNotEquals("foo", strategy.getValue("bar", "[0]").orElse(null));

        // Test does not equal
        Assert.assertNotEquals("bar", strategy.getValue(arr, "[0]").orElse(null));
    }
}