/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test the public utility class
 *
 * @author zhouss
 * @since 2022-06-29
 */
public class CommonUtilsTest {
    private static final long SLEEP_TIME = 1000L;

    private static final long DIFF = 20L;

    private static final int TEST_PORT = 8991;

    /**
     * Sleep test
     */
    @Test
    public void testSleep() {
        final long start = System.currentTimeMillis();
        CommonUtils.sleep(SLEEP_TIME);
        final long end = System.currentTimeMillis();

        // There is an error, and the error value is 20 ms before and after
        assertTrue((end - start < SLEEP_TIME + DIFF) || (end - start > SLEEP_TIME - DIFF));
    }

    /**
     * Test consumer spending power
     */
    @Test
    public void testConsumer() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        CommonUtils.accept(param -> atomicBoolean.set(param == SLEEP_TIME), SLEEP_TIME);
        assertTrue(atomicBoolean.get());
    }

    /**
     * The test gets information from the endpoint
     */
    @Test
    public void testGetByEndpoint() {
        String endpoint = "rest://127.0.0.1:8991";
        final Optional<String> ipByEndpoint = CommonUtils.getIpByEndpoint(endpoint);
        Assert.assertTrue(ipByEndpoint.isPresent());
        Assert.assertEquals(ipByEndpoint.get(), "127.0.0.1");
        final int portByEndpoint = CommonUtils.getPortByEndpoint(endpoint);
        Assert.assertEquals(TEST_PORT, portByEndpoint);
    }
}
