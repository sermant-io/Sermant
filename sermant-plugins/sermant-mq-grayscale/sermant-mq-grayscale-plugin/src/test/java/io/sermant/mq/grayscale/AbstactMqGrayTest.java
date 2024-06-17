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

package io.sermant.mq.grayscale;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class AbstactMqGrayTest {
    private MockedStatic<ConfigManager> configManagerMockedStatic;

    @Before
    public void setUp() {
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        ServiceMeta serviceMeta = new ServiceMeta();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("test", "gray");
        serviceMeta.setParameters(parameters);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(serviceMeta);
    }

    @After
    public void tearDown() {
        configManagerMockedStatic.close();
    }
}
