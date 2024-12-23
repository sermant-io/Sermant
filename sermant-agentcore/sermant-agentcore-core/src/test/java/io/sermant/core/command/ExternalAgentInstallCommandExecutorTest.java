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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.event.collector.FrameworkEventCollector;
import io.sermant.core.event.config.EventConfig;
import io.sermant.core.ext.ExternalAgentManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit Test for ExternalAgentInstallCommandExecutor
 *
 * @author lilai
 * @since 2024-12-18
 */
public class ExternalAgentInstallCommandExecutorTest {
    private MockedStatic<DynamicAgentArgsManager> dynamicAgentArgsManagerMockedStatic = mockStatic(
            DynamicAgentArgsManager.class);

    private Instrumentation mockInstrumentation = mock(Instrumentation.class);

    private MockedStatic<CommandProcessor> commandProcessorMock = mockStatic(CommandProcessor.class);

    private MockedStatic<ExternalAgentManager> otelAgentManagerMock = mockStatic(ExternalAgentManager.class);

    private MockedStatic<FrameworkEventCollector> frameworkEventCollectorMock = mockStatic(
            FrameworkEventCollector.class);

    private FrameworkEventCollector mockEventCollector = mock(FrameworkEventCollector.class);

    private Map<String, String> mockArgsMap = new HashMap<>();

    private MockedStatic<ConfigManager> configManagerMock = mockStatic(ConfigManager.class);

    @Before
    public void setUp() throws Exception {
        EventConfig eventConfig = new EventConfig();
        eventConfig.setEnable(false);
        configManagerMock.when(() -> ConfigManager.getConfig(EventConfig.class)).thenReturn(eventConfig);
        dynamicAgentArgsManagerMockedStatic.when(DynamicAgentArgsManager::getAgentArgsMap).thenReturn(mockArgsMap);
        commandProcessorMock.when(CommandProcessor::getInstrumentation).thenReturn(mockInstrumentation);
        frameworkEventCollectorMock.when(FrameworkEventCollector::getInstance).thenReturn(mockEventCollector);
    }

    @After
    public void tearDown() throws Exception {
        commandProcessorMock.close();
        otelAgentManagerMock.close();
        frameworkEventCollectorMock.close();
        mockArgsMap.clear();
        configManagerMock.close();
        dynamicAgentArgsManagerMockedStatic.close();
    }

    @Test
    public void testExecute_Success() {
        mockArgsMap.put("AGENT_FILE", "/path/to/otel-agent.jar");

        // Test instance
        ExternalAgentInstallCommandExecutor executor = new ExternalAgentInstallCommandExecutor();

        // Execute test
        executor.execute("OTEL");

        // Verify ExternalAgentManager.installExternalAgent was called
        otelAgentManagerMock.verify(() -> ExternalAgentManager.installExternalAgent(
                true,
                "OTEL",
                "/path/to/otel-agent.jar",
                mockArgsMap,
                mockInstrumentation
        ), times(1));
    }

    @Test
    public void testExecute_failure() {
        mockArgsMap.put("WRONG_FILE", "/path/to/otel-agent.jar");

        // Test instance
        ExternalAgentInstallCommandExecutor executor = new ExternalAgentInstallCommandExecutor();

        // Execute test
        executor.execute("OTEL");

        // Verify ExternalAgentManager.installExternalAgent was called
        otelAgentManagerMock.verify(() -> ExternalAgentManager.installExternalAgent(
                true,
                "OTEL",
                "/path/to/otel-agent.jar",
                mockArgsMap,
                mockInstrumentation
        ), times(0));
    }
}
