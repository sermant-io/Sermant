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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class IpMatcherTest {
    private IpMatcher ipMatcher;

    @Before
    public void setUp() {
        ipMatcher = new IpMatcher(24, "192.168.1.1");
    }

    // Constructor tests
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativePrefixLength() {
        new IpMatcher(-1, "192.168.1.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithTooLargePrefixLength() {
        new IpMatcher(33, "192.168.1.1");
    }

    @Test
    public void testConstructorWithValidPrefixLength() {
        new IpMatcher(0, "192.168.1.1");
        new IpMatcher(32, "192.168.1.1");
    }

    // match method tests
    @Test
    public void testMatchWithNullInput() {
        assertFalse(ipMatcher.match(null));
    }

    @Test
    public void testMatchWithEmptyInput() {
        assertFalse(ipMatcher.match(""));
    }

    @Test
    public void testMatchWithInvalidIp() {
        assertFalse(ipMatcher.match("invalid.ip"));
    }

    @Test
    public void testMatchWithPrefixLengthZero() {
        IpMatcher matcher = new IpMatcher(0, "192.168.1.1");
        assertTrue(matcher.match("192.168.1.1"));
        assertFalse(matcher.match("192.168.1.2"));
    }

    @Test
    public void testMatchWithPrefixLengthGreaterThanZero() {
        // Same prefix
        assertTrue(ipMatcher.match("192.168.1.100"));
        // Different prefix
        assertFalse(ipMatcher.match("192.168.2.1"));
    }

    @Test
    public void testMatchWithDifferentLengthIps() {
        IpMatcher matcher = new IpMatcher(16, "192.168.1.1");
        assertTrue(matcher.match("192.168.255.255"));
    }

    @Test
    public void testEqualsSymmetric() {
        IpMatcher another = new IpMatcher(24, "192.168.1.1");
        assertEquals(ipMatcher, another);
        assertEquals(another, ipMatcher);
    }

    @Test
    public void testEqualsConsistent() {
        IpMatcher another = new IpMatcher(24, "192.168.1.1");
        assertEquals(ipMatcher, another);
        assertEquals(ipMatcher, another); // Multiple calls
    }

    @Test
    public void testEqualsWithNull() {
        assertNotEquals(ipMatcher, null);
    }

    @Test
    public void testEqualsWithDifferentClass() {
        assertNotEquals(ipMatcher, "not an IpMatcher");
    }

    @Test
    public void testEqualsWithDifferentPrefixLength() {
        IpMatcher different = new IpMatcher(16, "192.168.1.1");
        assertNotEquals(ipMatcher, different);
    }

    @Test
    public void testEqualsWithDifferentIp() {
        IpMatcher different = new IpMatcher(24, "192.168.1.2");
        assertNotEquals(ipMatcher, different);
    }

    @Test
    public void testHashCodeConsistent() {
        int initialHashCode = ipMatcher.hashCode();
        assertEquals(initialHashCode, ipMatcher.hashCode());
    }

    @Test
    public void testHashCodeEqualObjects() {
        IpMatcher another = new IpMatcher(24, "192.168.1.1");
        assertEquals(ipMatcher.hashCode(), another.hashCode());
    }
}
