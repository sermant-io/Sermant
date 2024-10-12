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

package io.sermant.core.service.xds.entity;

import io.sermant.core.service.xds.entity.match.ExactMatchStrategy;
import io.sermant.core.service.xds.entity.match.MatchStrategy;

import org.junit.Assert;
import org.junit.Test;

/**
 * XdsPathMatcherTest
 *
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class XdsPathMatcherTest {
    @Test
    public void testIsMatchCaseSensitiveMatching() {
        MatchStrategy matchStrategy = new ExactMatchStrategy("/test/path");
        XdsPathMatcher matcher = new XdsPathMatcher(matchStrategy, true);
        Assert.assertTrue(matcher.isCaseSensitive());
        Assert.assertTrue(matcher.isMatch("/test/path"));
        Assert.assertFalse(matcher.isMatch("/TEST/PATH"));
    }

    @Test
    public void testIsMatchCaseInsensitiveMatching() {
        MatchStrategy matchStrategy = new ExactMatchStrategy("/test/path");
        XdsPathMatcher matcher = new XdsPathMatcher(matchStrategy, false);
        Assert.assertFalse(matcher.isCaseSensitive());
        Assert.assertTrue(matcher.isMatch("/test/path"));
        Assert.assertTrue(matcher.isMatch("/TEST/PATH"));
    }

    @Test
    public void testIsMatchWithNullPath() {
        MatchStrategy matchStrategy = new ExactMatchStrategy("/test/path");
        XdsPathMatcher matcher = new XdsPathMatcher(matchStrategy, true);
        Assert.assertFalse(matcher.isMatch(null));
    }

    @Test
    public void testIsMatchWithEmptyPath() {
        MatchStrategy matchStrategy = new ExactMatchStrategy("");
        XdsPathMatcher matcher = new XdsPathMatcher(matchStrategy, true);

        Assert.assertTrue(matcher.isMatch(""));
        Assert.assertFalse(matcher.isMatch("/nonempty/path"));
    }
}
