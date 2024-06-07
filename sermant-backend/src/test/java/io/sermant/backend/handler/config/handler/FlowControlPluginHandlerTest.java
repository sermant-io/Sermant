/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.backend.handler.config.handler;

import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.PluginType;
import io.sermant.backend.handler.config.FlowControlPluginHandler;
import io.sermant.backend.handler.config.PluginConfigHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class for FlowControlPluginHandler class
 *
 * @author zhp
 * @since 2024-06-05
 */
@SpringBootTest
public class FlowControlPluginHandlerTest {
    private static final String DEFAULT_SERVICE_NAME = "provider";

    private static final String DEFAULT_GROUP = "service=provider";

    private static final String ERROR_GROUP = "service=provider&app=default";

    private static final String ERROR_KEY = "testKey";

    private static final String RATE_LIMIT_CONFIGURATION_NAME = "servicecomb.rateLimiting.test";

    private static final String BULKHEAD_CONFIGURATION_NAME = "servicecomb.bulkhead.test";

    private static final String MATCH_GROUP_CONFIGURATION_NAME = "servicecomb.matchGroup.test";

    private static final String CIRCUIT_BREAKER_CONFIGURATION_NAME = "servicecomb.circuitBreaker.bulkhead";

    private static final String FAULT_INJECTION_CONFIGURATION_NAME = "servicecomb.faultInjection.test";

    private static final String RETRY_CONFIGURATION_NAME = "servicecomb.retry.test";

    private static final String SYSTEM_CONFIGURATION_NAME = "servicecomb.system.bulkhead";

    @Test
    public void parsePluginInfo() {
        PluginConfigHandler handler = new FlowControlPluginHandler();
        ConfigInfo configInfo = handler.parsePluginInfo(RATE_LIMIT_CONFIGURATION_NAME, DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getServiceName(), DEFAULT_SERVICE_NAME);
        Assert.assertEquals(configInfo.getKey(), RATE_LIMIT_CONFIGURATION_NAME);
        Assert.assertEquals(configInfo.getGroup(), DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getPluginType(), PluginType.FLOW_CONTROL.getPluginName());
    }

    @Test
    public void verifyConfiguration() {
        PluginConfigHandler handler = new FlowControlPluginHandler();
        Assert.assertTrue(handler.verifyConfigurationKey(RATE_LIMIT_CONFIGURATION_NAME));
        Assert.assertTrue(handler.verifyConfigurationKey(BULKHEAD_CONFIGURATION_NAME));
        Assert.assertTrue(handler.verifyConfigurationKey(MATCH_GROUP_CONFIGURATION_NAME));
        Assert.assertTrue(handler.verifyConfigurationKey(CIRCUIT_BREAKER_CONFIGURATION_NAME));
        Assert.assertTrue(handler.verifyConfigurationKey(FAULT_INJECTION_CONFIGURATION_NAME));
        Assert.assertTrue(handler.verifyConfigurationKey(RETRY_CONFIGURATION_NAME));
        Assert.assertTrue(handler.verifyConfigurationKey(SYSTEM_CONFIGURATION_NAME));
        Assert.assertTrue(handler.verifyConfigurationKey(RATE_LIMIT_CONFIGURATION_NAME));
        Assert.assertFalse(handler.verifyConfigurationKey(ERROR_KEY));
        Assert.assertTrue(handler.verifyConfigurationGroup(DEFAULT_GROUP));
        Assert.assertFalse(handler.verifyConfigurationKey(ERROR_GROUP));
    }
}