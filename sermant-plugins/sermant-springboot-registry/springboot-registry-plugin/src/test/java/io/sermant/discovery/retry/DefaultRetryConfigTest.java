/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.discovery.retry;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.discovery.config.LbConfig;
import io.sermant.discovery.retry.config.DefaultRetryConfig;
import io.sermant.discovery.retry.config.RetryConfig;

import org.apache.http.conn.ConnectTimeoutException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * Retry test
 *
 * @author zhouss
 * @since 2022-10-14
 */
public class DefaultRetryConfigTest {
    private final LbConfig lbConfig = new LbConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(lbConfig);
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void testExPredicate() {
        lbConfig.setSpecificExceptionsForRetry(
                Collections.singletonList("io.sermant.discovery.retry.DefaultRetryConfigTest$Ex"));
        final RetryConfig retryConfig = DefaultRetryConfig.create();
        Assert.assertTrue(retryConfig.getThrowablePredicate().test(new ConnectTimeoutException()));
        // Subclasses
        Assert.assertTrue(retryConfig.getThrowablePredicate().test(new MyEx()));
        Assert.assertTrue(retryConfig.getThrowablePredicate().test(new MyInter()));
    }

    static class MyEx extends ConnectTimeoutException {

    }

    interface Ex {
    }

    static class MyInter extends Exception implements Ex {

    }
}
