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
 * SuffixMatchStrategyTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class SuffixMatchStrategyTest {
    private SuffixMatchStrategy strategy;

    @Test
    public void testIsMatchWithValidSuffix() {
        strategy = new SuffixMatchStrategy("suffix");
        Assert.assertTrue(strategy.isMatch("testsuffix"));
        Assert.assertFalse(strategy.isMatch("suffixTest"));
    }

    @Test
    public void testIsMatchWithNullSuffix() {
        strategy = new SuffixMatchStrategy(null);
        Assert.assertTrue(strategy.isMatch("anyValue"));
        Assert.assertTrue(strategy.isMatch(""));
    }

    @Test
    public void testIsMatchWithEmptySuffix() {
        strategy = new SuffixMatchStrategy("");
        Assert.assertTrue(strategy.isMatch("anyValue"));
        Assert.assertTrue(strategy.isMatch(""));
        Assert.assertFalse(strategy.isMatch(null));
    }

    @Test
    public void testIsMatchWithNullRequestValue() {
        strategy = new SuffixMatchStrategy("suffix");
        Assert.assertFalse(strategy.isMatch(null));
    }

    @Test
    public void testIsMatchWithExactMatch() {
        strategy = new SuffixMatchStrategy("exact");
        Assert.assertTrue(strategy.isMatch("exact"));
    }

    @Test
    public void testIsMatchWithNonMatchingSuffix() {
        strategy = new SuffixMatchStrategy("suffix");
        Assert.assertFalse(strategy.isMatch("test"));
    }
}
