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

package io.sermant.core.command;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.PluginManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.logging.Logger;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

/**
 * Unit Tests for  HotPluggingListener
 *
 * @author zhp
 * @since 2024-09-02
 */
public class PluginsUpdateCommandExecutorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final MockedStatic<PluginManager> PLUGIN_MANAGER = Mockito.mockStatic(PluginManager.class);

    private PluginsUpdateCommandExecutor pluginsUpdateCommandExecutor;

    @Before
    public void setUp() {
        pluginsUpdateCommandExecutor = new PluginsUpdateCommandExecutor();
    }

    @Test
    public void testExecuteWithEmptyArgs() {
        pluginsUpdateCommandExecutor.execute("");
        PLUGIN_MANAGER.verify(() -> PluginManager.install(any()), times(0));
    }

    @After
    public void closeMock() {
        PLUGIN_MANAGER.close();
    }
}
