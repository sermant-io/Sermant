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

package io.sermant.core.ext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.event.collector.FrameworkEventCollector;
import io.sermant.core.event.config.EventConfig;
import io.sermant.core.utils.ReflectUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit Test for ExternalAgentManager
 *
 * @author lilai
 * @since 2024-12-18
 */
public class ExternalAgentManagerTest {
    private final MockedStatic<ConfigManager> configManagerMockedStatic = Mockito.mockStatic(
            ConfigManager.class);

    private Instrumentation mockInstrumentation = mock(Instrumentation.class);

    private static final String AGENT_NAME = "demo-agent";

    private static final String AGENT_PATH = "/path/to/demo-agent.jar";

    private static final Map<String, String> ARGS_MAP = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(EventConfig.class)).thenReturn(new EventConfig());
        Optional<Object> eventConfigOptional = ReflectUtils.getFieldValue(FrameworkEventCollector.getInstance(),
                "eventConfig");
        EventConfig eventConfig = (EventConfig) (eventConfigOptional.get());
        eventConfig.setEnable(false);
    }

    @After
    public void tearDown() {
        ExternalAgentManager.getExternalAgentInstallationStatus().clear();
        configManagerMockedStatic.close();
    }

    @Test
    public void testGetAgentVersion() {
        String agentName = AGENT_NAME;
        ExternalAgentManager.setAgentVersion(agentName, "1.0.0");
        String version = ExternalAgentManager.getAgentVersion(agentName);
        Assert.assertEquals("1.0.0", version);
    }

    @Test
    public void testGetInstallationStatus() {
        Map<String, AtomicBoolean> externalAgentInstallationStatus = ExternalAgentManager.getExternalAgentInstallationStatus();
        externalAgentInstallationStatus.put(AGENT_NAME, new AtomicBoolean(true));
        boolean installationStatus = ExternalAgentManager.getInstallationStatus(AGENT_NAME);
        Assert.assertTrue(installationStatus);
    }

    @Test
    public void testInstallExternalAgent_installedSuccessful() throws Exception {
        boolean isDynamic = true;
        try (MockedStatic<ExternalAgentManager> mockedManager = mockStatic(ExternalAgentManager.class)) {
            mockedManager.when(
                    () -> ExternalAgentManager.installExternalAgent(isDynamic, AGENT_NAME, AGENT_PATH,
                            ARGS_MAP, mockInstrumentation)).thenCallRealMethod();
            ExternalAgentManager.installExternalAgent(isDynamic, AGENT_NAME, AGENT_PATH, ARGS_MAP,
                    mockInstrumentation);
            mockedManager.verify(
                    () -> ExternalAgentManager.installAgent(isDynamic, AGENT_NAME, AGENT_PATH,
                            ARGS_MAP, mockInstrumentation), times(1));
        }
    }

    @Test
    public void testInstallExternalAgent_alreadyInstalled() throws Exception {
        String agentName = AGENT_NAME;
        AtomicBoolean status = new AtomicBoolean(true);
        Map<String, AtomicBoolean> externalAgentInstallationStatus = ExternalAgentManager.getExternalAgentInstallationStatus();
        externalAgentInstallationStatus.put(agentName, status);
        ExternalAgentManager.installExternalAgent(true, AGENT_NAME, AGENT_PATH, ARGS_MAP, mockInstrumentation);
        verify(mockInstrumentation, never()).appendToSystemClassLoaderSearch(any());
    }

    @Test
    public void testSetArgsToSystemProperties() {
        ARGS_MAP.put("otel.javaagent.debug", "true");
        ExternalAgentManager.setArgsToSystemProperties(ARGS_MAP);
        Assert.assertEquals("true", System.getProperty("otel.javaagent.debug"));
    }
}
