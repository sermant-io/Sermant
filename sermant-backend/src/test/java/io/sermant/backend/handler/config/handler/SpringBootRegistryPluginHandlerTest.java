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
import io.sermant.backend.handler.config.PluginConfigHandler;
import io.sermant.backend.handler.config.SpringBootRegistryPluginHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class for SpringBootRegistryPluginHandler class
 *
 * @author zhp
 * @since 2024-06-05
 */
@SpringBootTest
public class SpringBootRegistryPluginHandlerTest {
    private static final String DEFAULT_APP_NAME = "default";

    private static final String DEFAULT_ENVIRONMENT_NAME = "prod";

    private static final String DEFAULT_SERVICE_NAME = "provider";

    private static final String DEFAULT_GROUP = "app=default&environment=prod&service=provider";

    private static final String ERROR_GROUP = "app=default&env=prod&service=provider";

    private static final String ERROR_KEY = "testKey";

    private static final String CONFIGURATION_NAME = "sermant.plugin.registry";

    @Test
    public void parsePluginInfo() {
        PluginConfigHandler handler = new SpringBootRegistryPluginHandler();
        ConfigInfo configInfo = handler.parsePluginInfo(CONFIGURATION_NAME, DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getAppName(), DEFAULT_APP_NAME);
        Assert.assertEquals(configInfo.getEnvironment(), DEFAULT_ENVIRONMENT_NAME);
        Assert.assertEquals(configInfo.getServiceName(), DEFAULT_SERVICE_NAME);
        Assert.assertEquals(configInfo.getKey(), CONFIGURATION_NAME);
        Assert.assertEquals(configInfo.getGroup(), DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getPluginType(), PluginType.SPRINGBOOT_REGISTRY.getPluginName());
    }

    @Test
    public void verifyConfiguration() {
        PluginConfigHandler handler = new SpringBootRegistryPluginHandler();
        Assert.assertTrue(handler.verifyConfigurationKey(CONFIGURATION_NAME));
        Assert.assertFalse(handler.verifyConfigurationKey(ERROR_KEY));
        Assert.assertTrue(handler.verifyConfigurationGroup(DEFAULT_GROUP));
        Assert.assertFalse(handler.verifyConfigurationGroup(ERROR_GROUP));
    }
}