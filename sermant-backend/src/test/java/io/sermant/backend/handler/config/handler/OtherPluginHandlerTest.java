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
import io.sermant.backend.handler.config.OtherPluginHandler;
import io.sermant.backend.handler.config.PluginConfigHandler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class for OtherPluginHandler class
 *
 * @author zhp
 * @since 2024-06-05
 */
@SpringBootTest
public class OtherPluginHandlerTest {
    private static final String DEFAULT_KEY = "sermant.database.write.provider";

    private static final String DEFAULT_GROUP = "app=default&environment=prod&zone=gz";

    @Test
    public void parsePluginInfo() {
        PluginConfigHandler handler = new OtherPluginHandler();
        ConfigInfo configInfo = handler.parsePluginInfo(DEFAULT_KEY, DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getKey(), DEFAULT_KEY);
        Assert.assertEquals(configInfo.getGroup(), DEFAULT_GROUP);
        Assert.assertEquals(configInfo.getPluginType(), PluginType.OTHER.getPluginName());
    }

    @Test
    public void verifyConfiguration() {
        PluginConfigHandler handler = new OtherPluginHandler();
        Assert.assertTrue(handler.verifyConfigurationKey(DEFAULT_KEY));
        Assert.assertTrue(handler.verifyConfigurationGroup(DEFAULT_GROUP));
    }
}