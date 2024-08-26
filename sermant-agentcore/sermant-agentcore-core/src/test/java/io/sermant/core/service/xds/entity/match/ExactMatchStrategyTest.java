/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.core.service.xds.entity.match;

import org.junit.Assert;
import org.junit.Test;

/**
 * ExactMatchStrategyTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class ExactMatchStrategyTest {
    private ExactMatchStrategy strategy;

    @Test
    public void testConstructorWithNonNullValue() {
        String expectedValue = "test";
        strategy = new ExactMatchStrategy(expectedValue);
        Assert.assertEquals(expectedValue, strategy.isMatch("test") ? "test" : "");
    }

    @Test
    public void testConstructorWithNullValue() {
        strategy = new ExactMatchStrategy(null);
        Assert.assertFalse(strategy.isMatch("test"));
        Assert.assertTrue(strategy.isMatch(""));
    }

    @Test
    public void testIsMatchWithMatchingValue() {
        strategy = new ExactMatchStrategy("exact");
        Assert.assertTrue(strategy.isMatch("exact"));
    }

    @Test
    public void testIsMatchWithNonMatchingValue() {
        strategy = new ExactMatchStrategy("exact");
        Assert.assertFalse(strategy.isMatch("notExact"));
    }

    @Test
    public void testIsMatchWithEmptyValue() {
        strategy = new ExactMatchStrategy("");
        Assert.assertTrue(strategy.isMatch(""));
    }

    @Test
    public void testIsMatchWithNullRequestValue() {
        strategy = new ExactMatchStrategy("exact");
        boolean result = strategy.isMatch(null);
        Assert.assertFalse(strategy.isMatch(null));
    }
}