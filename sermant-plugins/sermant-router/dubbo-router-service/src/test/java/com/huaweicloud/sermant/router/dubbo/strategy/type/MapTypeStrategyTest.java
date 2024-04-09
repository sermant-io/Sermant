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

import java.util.HashMap;
import java.util.Map;

/**
 * map matching strategy test
 *
 * @author provenceee
 * @since 2021-12-01
 */
public class MapTypeStrategyTest {
    /**
     * test the map policy
     */
    @Test
    public void testValue() {
        TypeStrategy strategy = new MapTypeStrategy();
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");

        // normal
        Assert.assertEquals("bar", strategy.getValue(map, ".get(\"foo\")").orElse(null));

        // test null
        Assert.assertNotEquals("bar", strategy.getValue(map, ".get(\"bar\")").orElse(null));

        // test non map
        Assert.assertNotEquals("bar", strategy.getValue("foo", ".get(\"foo\")").orElse(null));

        // the test is not equal
        Assert.assertNotEquals("foo", strategy.getValue(map, ".get(\"foo\")").orElse(null));
    }
}