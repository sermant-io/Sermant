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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link PortMatcher}
 *
 * @since 2025-06-17
 */
@RunWith(MockitoJUnitRunner.class)
public class PortMatcherTest {
    private PortMatcher portMatcher;

    @Test
    public void testMatchWithSamePort() {
        portMatcher = new PortMatcher(8080);
        assertTrue(portMatcher.match(8080));
    }

    @Test
    public void testMatchWithDifferentPort() {
        portMatcher = new PortMatcher(8080);
        assertFalse(portMatcher.match(8081));
    }

    @Test
    public void testMatchWithMinPort() {
        portMatcher = new PortMatcher(0);
        assertTrue(portMatcher.match(0));
    }

    @Test
    public void testMatchWithMaxPort() {
        portMatcher = new PortMatcher(65535);
        assertTrue(portMatcher.match(65535));
    }

    @Test
    public void testEqualsWithNull() {
        portMatcher = new PortMatcher(8080);
        assertNotEquals(null, portMatcher);
    }

    @Test
    public void testEqualsWithDifferentClass() {
        portMatcher = new PortMatcher(8080);
        assertNotEquals("not a PortMatcher", portMatcher);
    }

    @Test
    public void testEqualsWithSamePort() {
        portMatcher = new PortMatcher(8080);
        PortMatcher other = new PortMatcher(8080);
        assertEquals(portMatcher, other);
    }

    @Test
    public void testEqualsWithDifferentPort() {
        portMatcher = new PortMatcher(8080);
        PortMatcher other = new PortMatcher(8081);
        assertNotEquals(portMatcher, other);
    }

    @Test
    public void testHashCodeConsistency() {
        portMatcher = new PortMatcher(8080);
        int initialHashCode = portMatcher.hashCode();
        assertEquals(initialHashCode, portMatcher.hashCode());
    }

    @Test
    public void testHashCodeWithSamePort() {
        portMatcher = new PortMatcher(8080);
        PortMatcher other = new PortMatcher(8080);
        assertEquals(portMatcher.hashCode(), other.hashCode());
    }

    @Test
    public void testHashCodeWithDifferentPort() {
        portMatcher = new PortMatcher(8080);
        PortMatcher other = new PortMatcher(8081);
        assertNotEquals(portMatcher.hashCode(), other.hashCode());
    }
}
