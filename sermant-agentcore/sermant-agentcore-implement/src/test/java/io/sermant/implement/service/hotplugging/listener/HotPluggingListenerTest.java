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

package io.sermant.implement.service.hotplugging.listener;

import io.sermant.core.command.CommandProcessor;
import io.sermant.core.common.BootArgsIndexer;
import io.sermant.core.common.CommonConstant;
import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.utils.JarFileUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.implement.operation.converter.YamlConverterImpl;
import io.sermant.implement.service.hotplugging.entity.HotPluggingConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


/**
 * Unit Tests for HotPluggingListener
 *
 * @author zhp
 * @since 2024-09-02
 */
public class HotPluggingListenerTest {
    private static final String CONTENT = "agentPath: ''\n" +
            "commandType: UNINSTALL-PLUGINS\n" +
            "instanceIds: d656cc1c-9951-4c82-9fb2-6dd77fb66faa\n" +
            "params: ''\n" +
            "pluginNames: database-write-prohibition\n";

    private static final String KEY = "config";

    private static final String GROUP = "sermant-hot-plugging";

    private static final YamlConverter YAML_CONVERTER = new YamlConverterImpl();

    private final MockedStatic<OperationManager> operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);

    private final MockedStatic<CommandProcessor> commandProcessorMockedStatic = Mockito.mockStatic(CommandProcessor.class);

    private final MockedStatic<JarFileUtils> jarFileUtilsMockedStatic = Mockito.mockStatic(JarFileUtils.class);

    private final MockedConstruction<JarFile> mockedConstruction = mockConstruction(JarFile.class,
            (mock, context) -> when(mock.getManifest()).thenReturn(new Manifest()));

    private final Yaml yaml = new Yaml();

    @Before
    public void setUp() throws Exception {
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
                .thenReturn(YAML_CONVERTER);
        commandProcessorMockedStatic.verify(() -> CommandProcessor.process(any()), times(0));
        jarFileUtilsMockedStatic.when(() -> JarFileUtils.getManifestAttr(any(), anyString())).thenReturn("1.0.0");
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put(CommonConstant.CORE_IMPLEMENT_DIR_KEY, StringUtils.EMPTY);
        argsMap.put(CommonConstant.CORE_CONFIG_FILE_KEY, StringUtils.EMPTY);
        argsMap.put(CommonConstant.PLUGIN_SETTING_FILE_KEY, StringUtils.EMPTY);
        argsMap.put(CommonConstant.LOG_SETTING_FILE_KEY, StringUtils.EMPTY);
        argsMap.put(CommonConstant.PLUGIN_PACKAGE_DIR_KEY, StringUtils.EMPTY);
        argsMap.put(CommonConstant.APP_NAME_KEY, StringUtils.EMPTY);
        argsMap.put(CommonConstant.APP_TYPE_KEY, StringUtils.EMPTY);
        argsMap.put(CommonConstant.SERVICE_NAME_KEY, StringUtils.EMPTY);
        argsMap.put(CommonConstant.ARTIFACT_NAME_KEY, StringUtils.EMPTY);
        BootArgsIndexer.build(argsMap, true);
    }

    @Test
    public void testHandleInitEvent() {
        HotPluggingListener hotPluggingListener = new HotPluggingListener();
        DynamicConfigEvent event = new DynamicConfigEvent(KEY, GROUP, CONTENT, DynamicConfigEventType.INIT);
        hotPluggingListener.process(event);
        commandProcessorMockedStatic.verify(() -> CommandProcessor.process(any()), times(0));
    }

    @Test
    public void testHandleDeleteEvent() {
        HotPluggingListener hotPluggingListener = new HotPluggingListener();
        DynamicConfigEvent event = new DynamicConfigEvent(KEY, GROUP, CONTENT, DynamicConfigEventType.DELETE);
        hotPluggingListener.process(event);
        commandProcessorMockedStatic.verify(() -> CommandProcessor.process(any()), times(0));
    }

    @Test
    public void testProcessWithEmptyConfig() {
        HotPluggingListener hotPluggingListener = new HotPluggingListener();
        DynamicConfigEvent event = new DynamicConfigEvent(KEY, GROUP, "", DynamicConfigEventType.CREATE);
        hotPluggingListener.process(event);
        commandProcessorMockedStatic.verify(() -> CommandProcessor.process(any()), times(0));
    }

    @Test
    public void testProcessWithConfigWithoutInstanceId() {
        HotPluggingListener hotPluggingListener = new HotPluggingListener();
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        hotPluggingConfig.setCommandType("UNINSTALL-PLUGINS");
        hotPluggingConfig.setInstanceIds("d656cc1c-9951-4c82-9fb2-6dd77fb66faa");
        hotPluggingConfig.setPluginNames("database-write-prohibition");
        DynamicConfigEvent event = new DynamicConfigEvent(KEY, GROUP, yaml.dumpAsMap(hotPluggingConfig),
                DynamicConfigEventType.CREATE);
        hotPluggingListener.process(event);
        commandProcessorMockedStatic.verify(() -> CommandProcessor.process(any()), times(0));
    }

    @Test
    public void testExecute() {
        HotPluggingListener hotPluggingListener = new HotPluggingListener();
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        hotPluggingConfig.setCommandType("UNINSTALL-PLUGINS");
        hotPluggingConfig.setInstanceIds(BootArgsIndexer.getInstanceId());
        hotPluggingConfig.setPluginNames("database-write-prohibition");
        DynamicConfigEvent event = new DynamicConfigEvent(KEY, GROUP, yaml.dumpAsMap(hotPluggingConfig),
                DynamicConfigEventType.CREATE);
        hotPluggingListener.process(event);
        commandProcessorMockedStatic.verify(() -> CommandProcessor.process(any()), times(1));
        commandProcessorMockedStatic.clearInvocations();
    }

    @After
    public void closeMock() {
        commandProcessorMockedStatic.close();
        operationManagerMockedStatic.close();
        mockedConstruction.close();
        jarFileUtilsMockedStatic.close();
    }
}