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

package io.sermant.flowcontrol.common.xds.ratelimit;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * xds rate limit information Testz
 *
 * @author zhp
 * @since 2024-12-05
 */
public class XdsRateLimitInfoTest {
    private XdsRateLimitInfo xdsRateLimitInfoUnderTest;

    @Before
    public void setUp() throws Exception {
        xdsRateLimitInfoUnderTest = new XdsRateLimitInfo(0L, 0);
    }

    @Test
    public void testLastFilledTimeGetterAndSetter() {
        final long lastFilledTime = 0L;
        xdsRateLimitInfoUnderTest.setLastFilledTime(lastFilledTime);
        assertEquals(lastFilledTime, xdsRateLimitInfoUnderTest.getLastFilledTime());
    }

    @Test
    public void testCurrentTokensGetterAndSetter() {
        final AtomicInteger currentTokens = new AtomicInteger(0);
        xdsRateLimitInfoUnderTest.setCurrentTokens(currentTokens);
        assertEquals(currentTokens, xdsRateLimitInfoUnderTest.getCurrentTokens());
    }
}
