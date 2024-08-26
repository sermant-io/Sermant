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

import java.util.HashMap;
import java.util.Map;

/**
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class XdsHeaderMatcherTest {
    private MatchStrategy matchStrategy;

    private XdsHeaderMatcher matcher;

    private Map<String, String> headers;

    @Test
    public void testIsMatchWithMatchingHeader() {
        matchStrategy = new ExactMatchStrategy("test");
        matcher = new XdsHeaderMatcher("testHeader", matchStrategy);
        headers = new HashMap<>();
        headers.put("testHeader", "test");
        Assert.assertTrue(matcher.isMatch(headers));
    }

    @Test
    public void testIsMatchWithNonMatchingHeader() {
        matchStrategy = new ExactMatchStrategy("test");
        matcher = new XdsHeaderMatcher("testHeader", matchStrategy);
        headers = new HashMap<>();
        headers.put("testHeader", "noMatchTest");
        Assert.assertFalse(matcher.isMatch(headers));
    }

    @Test
    public void testIsMatchWithMissingHeader() {
        matchStrategy = new ExactMatchStrategy("test");
        matcher = new XdsHeaderMatcher("testHeader", matchStrategy);
        headers = new HashMap<>();
        headers.put("otherHeader", "noMatchTest");
        Assert.assertFalse(matcher.isMatch(headers));
    }
}