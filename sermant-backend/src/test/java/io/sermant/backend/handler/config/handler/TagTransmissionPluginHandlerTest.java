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
import io.sermant.backend.handler.config.TagTransmissionPluginHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class for TagTransmissionPluginHandler class
 *
 * @author zhp
 * @since 2024-06-05
 */
@SpringBootTest
public class TagTransmissionPluginHandlerTest {
    private static final String CONFIGURATION_GROUP_NAME = "sermant/tag-transmission-plugin";

    private static final String CONFIGURATION_KEY_NAME = "tag-config";

    private static final String ERROR_KEY = "testKey";

    private static final String ERROR_GROUP = "app=default&env=prod&service=provider";

    @Test
    public void parsePluginInfo() {
        PluginConfigHandler handler = new TagTransmissionPluginHandler();
        ConfigInfo configInfo = handler.parsePluginInfo(CONFIGURATION_KEY_NAME, CONFIGURATION_GROUP_NAME);
        Assert.assertEquals(configInfo.getKey(), CONFIGURATION_KEY_NAME);
        Assert.assertEquals(configInfo.getGroup(), CONFIGURATION_GROUP_NAME);
        Assert.assertEquals(configInfo.getPluginType(), PluginType.TAG_TRANSMISSION.getPluginName());
    }

    @Test
    public void verifyConfiguration() {
        PluginConfigHandler handler = new TagTransmissionPluginHandler();
        Assert.assertTrue(handler.verifyConfigurationKey(CONFIGURATION_KEY_NAME));
        Assert.assertFalse(handler.verifyConfigurationKey(ERROR_KEY));
        Assert.assertTrue(handler.verifyConfigurationGroup(CONFIGURATION_GROUP_NAME));
        Assert.assertFalse(handler.verifyConfigurationGroup(ERROR_GROUP));
    }
}