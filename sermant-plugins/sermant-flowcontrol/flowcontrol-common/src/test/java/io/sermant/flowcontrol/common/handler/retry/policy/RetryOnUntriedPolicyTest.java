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

package io.sermant.flowcontrol.common.handler.retry.policy;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * RetryOnUntriedPolicy Test
 *
 * @author zhp
 * @since 2024-11-28
 */
public class RetryOnUntriedPolicyTest {

    private RetryOnUntriedPolicy retryOnUntriedPolicy;

    @Before
    public void setUp() throws Exception {
        retryOnUntriedPolicy = new RetryOnUntriedPolicy(1);
    }

    @Test
    public void testNeedRetry() {
        assertTrue(retryOnUntriedPolicy.isReachedRetryThreshold());
    }

    @Test
    public void testIsRetry() {
        assertFalse(retryOnUntriedPolicy.isRetry());
    }

    @Test
    public void testRetryMark() {
        retryOnUntriedPolicy.retryMark();
        assertTrue(retryOnUntriedPolicy.isRetry());
        retryOnUntriedPolicy.retryMark();
        assertFalse(retryOnUntriedPolicy.isReachedRetryThreshold());
    }

    @Test
    public void testGetAllRetriedInstance() {
        final Set<Object> result = retryOnUntriedPolicy.getAllRetriedInstance();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testUpdateRetriedInstance() {
        retryOnUntriedPolicy.updateRetriedInstance("instance");
        final Set<Object> result = retryOnUntriedPolicy.getAllRetriedInstance();
        assertEquals(1, result.size());
    }
}
