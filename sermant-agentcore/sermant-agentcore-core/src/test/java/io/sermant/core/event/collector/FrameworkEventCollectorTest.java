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

package io.sermant.core.event.collector;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.event.Event;
import io.sermant.core.event.config.EventConfig;
import io.sermant.core.utils.JarFileUtils;
import io.sermant.core.utils.ReflectUtils;
import org.junit.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for FrameworkEventCollector
 *
 * @author zhp
 * @since 2024-09-02
 */
public class FrameworkEventCollectorTest {
    private static final MockedStatic<ConfigManager> CONFIG_MANAGER_MOCKED_STATIC = Mockito.mockStatic(ConfigManager.class);

    private static final MockedConstruction<JarFile> MOCKED_CONSTRUCTION = mockConstruction(JarFile.class,
            (mock, context) -> when(mock.getManifest()).thenReturn(new Manifest()));

    private static final MockedStatic<JarFileUtils> JAR_FILE_UTILS_MOCKED_STATIC = Mockito.mockStatic(JarFileUtils.class);

    private static final EventConfig EVENT_CONFIG = new EventConfig();
    
    @BeforeClass
    public static void setUp() throws Exception {
        CONFIG_MANAGER_MOCKED_STATIC.when(() -> ConfigManager.getConfig(EventConfig.class)).thenReturn(EVENT_CONFIG);
        JAR_FILE_UTILS_MOCKED_STATIC.when(() -> JarFileUtils.getManifestAttr(any(), anyString())).thenReturn("1.0.0");
    }

    @Test
    public void testEventNotEnabled() {
        EVENT_CONFIG.setEnable(false);
        FrameworkEventCollector.getInstance().collectdHotPluggingEvent(FrameworkEventDefinitions.SERMANT_PLUGIN_UNINSTALL,
                "Hot plugging command[INSTALL-PLUGINS] has been processed.");
        Optional<Object> optional = ReflectUtils.getFieldValue(FrameworkEventCollector.getInstance(), "eventQueue");
        Assert.assertTrue(optional.isPresent());
        BlockingQueue<Event> eventQueue = (BlockingQueue<Event>) optional.get();
        Assert.assertTrue(eventQueue.isEmpty());
    }

    @Test
    public void testEventEnabled() {
        EVENT_CONFIG.setEnable(true);
        FrameworkEventCollector.getInstance().collectdHotPluggingEvent(FrameworkEventDefinitions.SERMANT_PLUGIN_UNINSTALL,
                "Hot plugging command[INSTALL-PLUGINS] has been processed.");
        Optional<Object> optional = ReflectUtils.getFieldValue(FrameworkEventCollector.getInstance(), "eventQueue");
        Assert.assertTrue(optional.isPresent());
        BlockingQueue<Event> eventQueue = (BlockingQueue<Event>) optional.get();
        Assert.assertFalse(eventQueue.isEmpty());
        eventQueue.clear();
    }

    @AfterClass
    public static void closeMock() {
        CONFIG_MANAGER_MOCKED_STATIC.close();
        MOCKED_CONSTRUCTION.close();
        JAR_FILE_UTILS_MOCKED_STATIC.close();
    }
}