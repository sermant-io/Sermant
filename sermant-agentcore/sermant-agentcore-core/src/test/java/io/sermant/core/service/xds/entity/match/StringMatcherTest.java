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

package io.sermant.core.service.xds.entity.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringMatcherTest {
    // Test constructor with null or empty pattern
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullPattern() {
        new StringMatcher(null, StringMatcher.MatcherStrategy.EXACT, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyPattern() {
        new StringMatcher("", StringMatcher.MatcherStrategy.EXACT, false);
    }

    // Test exact match strategy
    @Test
    public void testExactMatchCaseSensitive() {
        StringMatcher matcher = new StringMatcher("Hello", StringMatcher.MatcherStrategy.EXACT, false);
        assertTrue(matcher.match("Hello"));
        assertFalse(matcher.match("hello"));
        assertFalse(matcher.match("HelloWorld"));
    }

    @Test
    public void testExactMatchCaseInsensitive() {
        StringMatcher matcher = new StringMatcher("Hello", StringMatcher.MatcherStrategy.EXACT, true);
        assertTrue(matcher.match("Hello"));
        assertTrue(matcher.match("hello"));
        assertFalse(matcher.match("HelloWorld"));
    }

    // Test prefix match strategy
    @Test
    public void testPrefixMatchCaseSensitive() {
        StringMatcher matcher = new StringMatcher("Hello", StringMatcher.MatcherStrategy.PREFIX, false);
        assertTrue(matcher.match("HelloWorld"));
        assertTrue(matcher.match("Hello"));
        assertFalse(matcher.match("helloWorld"));
        assertFalse(matcher.match("WorldHello"));
    }

    @Test
    public void testPrefixMatchCaseInsensitive() {
        StringMatcher matcher = new StringMatcher("Hello", StringMatcher.MatcherStrategy.PREFIX, true);
        assertTrue(matcher.match("HelloWorld"));
        assertTrue(matcher.match("helloWorld"));
        assertFalse(matcher.match("WorldHello"));
    }

    // Test suffix match strategy
    @Test
    public void testSuffixMatchCaseSensitive() {
        StringMatcher matcher = new StringMatcher("World", StringMatcher.MatcherStrategy.SUFFIX, false);
        assertTrue(matcher.match("HelloWorld"));
        assertTrue(matcher.match("World"));
        assertFalse(matcher.match("HelloWorlds"));
        assertFalse(matcher.match("world"));
    }

    @Test
    public void testSuffixMatchCaseInsensitive() {
        StringMatcher matcher = new StringMatcher("World", StringMatcher.MatcherStrategy.SUFFIX, true);
        assertTrue(matcher.match("HelloWorld"));
        assertTrue(matcher.match("Helloworld"));
        assertFalse(matcher.match("HelloWorlds"));
    }

    // Test contain match strategy
    @Test
    public void testContainMatchCaseSensitive() {
        StringMatcher matcher = new StringMatcher("llo", StringMatcher.MatcherStrategy.CONTAIN, false);
        assertTrue(matcher.match("Hello"));
        assertTrue(matcher.match("HelloWorld"));
        assertFalse(matcher.match("HeLLo"));
        assertFalse(matcher.match("World"));
    }

    @Test
    public void testContainMatchCaseInsensitive() {
        StringMatcher matcher = new StringMatcher("llo", StringMatcher.MatcherStrategy.CONTAIN, true);
        assertTrue(matcher.match("Hello"));
        assertTrue(matcher.match("HeLLo"));
        assertFalse(matcher.match("World"));
    }

    // Test regex match strategy
    @Test
    public void testRegexMatch() {
        StringMatcher matcher = new StringMatcher("^[A-Za-z]+$", StringMatcher.MatcherStrategy.REGEX, false);
        assertTrue(matcher.match("Hello"));
        assertFalse(matcher.match("Hello123"));
    }

    @Test
    public void testInvalidRegexMatch() {
        StringMatcher matcher = new StringMatcher("[", StringMatcher.MatcherStrategy.REGEX, false);
        assertFalse(matcher.match("AnyString"));
    }

    // Test empty/null input
    @Test
    public void testMatchWithEmptyInput() {
        StringMatcher matcher = new StringMatcher("pattern", StringMatcher.MatcherStrategy.EXACT, false);
        assertFalse(matcher.match(null));
        assertFalse(matcher.match(""));
    }

    // Test getter methods
    @Test
    public void testGetters() {
        StringMatcher matcher = new StringMatcher("Test", StringMatcher.MatcherStrategy.PREFIX, true);
        assertEquals("test", matcher.getPattern());
        assertEquals(StringMatcher.MatcherStrategy.PREFIX, matcher.getStrategy());
    }
}
