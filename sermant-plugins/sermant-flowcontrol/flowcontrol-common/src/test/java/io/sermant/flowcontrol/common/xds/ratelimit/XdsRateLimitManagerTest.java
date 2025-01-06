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

import io.sermant.core.service.xds.entity.XdsTokenBucket;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * RateLimitManager Test
 *
 * @author zhp
 * @since 2024-12-02
 */
public class XdsRateLimitManagerTest {
    private static final String SERVICE_NAME = "serviceA";

    private static final String ROUTE_NAME = "routeA";

    @Test
    public void testFillAndConsumeToken() throws InterruptedException {
        final XdsTokenBucket tokenBucket = new XdsTokenBucket();
        tokenBucket.setMaxTokens(1);
        tokenBucket.setTokensPerFill(1);
        tokenBucket.setFillInterval(1000L);
        boolean result = XdsRateLimitManager.fillAndConsumeToken(SERVICE_NAME, ROUTE_NAME, tokenBucket);
        assertTrue(result);

        // The situation where all tokens have been consumed
        result = XdsRateLimitManager.fillAndConsumeToken(SERVICE_NAME, ROUTE_NAME, tokenBucket);
        assertFalse(result);
        Thread.sleep(1000L);

        // Test token refill situation
        result = XdsRateLimitManager.fillAndConsumeToken(SERVICE_NAME, ROUTE_NAME, tokenBucket);
        assertTrue(result);

        // Test all refilled tokens have been consumed
        result = XdsRateLimitManager.fillAndConsumeToken(SERVICE_NAME, ROUTE_NAME, tokenBucket);
        assertFalse(result);
    }
}
