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
 * PrefixMatchStrategyTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class PrefixMatchStrategyTest {
    private PrefixMatchStrategy strategy;

    @Test
    public void testConstructorWithNonNullPrefix() {
        String expectedPrefix = "test";
        strategy = new PrefixMatchStrategy(expectedPrefix);
        Assert.assertTrue(strategy.isMatch("testValue"));
    }

    @Test
    public void testConstructorWithNullPrefix() {
        strategy = new PrefixMatchStrategy(null);
        Assert.assertTrue(strategy.isMatch("anyValue"));
        Assert.assertTrue(strategy.isMatch(""));
    }

    @Test
    public void testIsMatchWithMatchingPrefix() {
        strategy = new PrefixMatchStrategy("pre");
        Assert.assertTrue(strategy.isMatch("prefixValue"));
    }

    @Test
    public void testIsMatchWithNonMatchingPrefix() {
        strategy = new PrefixMatchStrategy("pre");
        Assert.assertFalse(strategy.isMatch("valuePrefix"));
    }

    @Test
    public void testIsMatchWithEmptyPrefix() {
        strategy = new PrefixMatchStrategy("");
        Assert.assertTrue(strategy.isMatch("anyValue"));
        Assert.assertTrue(strategy.isMatch(""));
    }

    @Test
    public void testIsMatchWithNullRequestValue() {
        strategy = new PrefixMatchStrategy("pre");
        Assert.assertFalse(strategy.isMatch(null));
    }

    @Test
    public void testIsMatchWithExactMatch() {
        strategy = new PrefixMatchStrategy("exact");
        Assert.assertTrue(strategy.isMatch("exact"));
    }
}
