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
 * RegexMatchStrategyTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class RegexMatchStrategyTest {
    private RegexMatchStrategy strategy;

    @Test
    public void testIsMatchWithValidRegex() {
        strategy = new RegexMatchStrategy("^[a-z]+$");
        Assert.assertTrue(strategy.isMatch("abc"));
        Assert.assertFalse(strategy.isMatch("abc123"));
    }

    @Test
    public void testIsMatchWithNullRegex() {
        strategy = new RegexMatchStrategy(null);
        Assert.assertTrue(strategy.isMatch("anyValue"));
        Assert.assertTrue(strategy.isMatch(""));
    }

    @Test
    public void testIsMatchWithEmptyRegex() {
        strategy = new RegexMatchStrategy("");
        Assert.assertTrue(strategy.isMatch("anyValue"));
        Assert.assertTrue(strategy.isMatch(""));
        Assert.assertFalse(strategy.isMatch(null));
    }

    @Test
    public void testIsMatchWithNullRequestValue() {
        strategy = new RegexMatchStrategy(".*");
        Assert.assertFalse(strategy.isMatch(null));
    }

    @Test
    public void testIsMatchWithComplexRegex() {
        strategy = new RegexMatchStrategy("^\\d{3}-\\d{2}-\\d{4}$");
        Assert.assertTrue(strategy.isMatch("123-45-6789"));
        Assert.assertFalse(strategy.isMatch("123-456-789"));
    }
}
