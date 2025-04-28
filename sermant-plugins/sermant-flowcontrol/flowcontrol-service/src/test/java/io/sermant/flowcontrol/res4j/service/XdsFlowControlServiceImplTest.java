/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
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


package io.sermant.flowcontrol.res4j.service;

import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.config.XdsFlowControlConfig;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import io.sermant.flowcontrol.res4j.chain.context.ChainContext;
import io.sermant.flowcontrol.service.rest4j.XdsHttpFlowControlService;
import io.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * XdsFlowControlServiceImpl test
 *
 * @author zhp
 * @since 2025-04-12
 */
public class XdsFlowControlServiceImplTest {
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        XdsFlowControlConfig xdsFlowControlConfig = new XdsFlowControlConfig();
        xdsFlowControlConfig.setEnable(true);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(XdsFlowControlConfig.class))
                .thenReturn(xdsFlowControlConfig);
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    /**
     * test process
     */
    @Test
    public void test() {
        final XdsHttpFlowControlService xdsHttpFlowControlService = new XdsHttpFlowControlServiceImpl();
        final FlowControlResult flowControlResult = new FlowControlResult();
        final FlowControlScenario flowControlScenario = new FlowControlScenario();
        HttpRequestEntity httpRequestEntity = new HttpRequestEntity();
        httpRequestEntity.setRequestType(RequestEntity.RequestType.CLIENT);
        xdsHttpFlowControlService.onBefore(httpRequestEntity, flowControlResult);
        xdsHttpFlowControlService.onThrow(httpRequestEntity, new Exception("error"), flowControlScenario);
        xdsHttpFlowControlService.onAfter(httpRequestEntity, new Object(), flowControlScenario);
        Assert.assertNotNull(XdsThreadLocalUtil.getScenarioInfo());
    }

    @After
    public void clear() {
        pluginConfigManagerMockedStatic.close();
        operationManagerMockedStatic.close();
        ChainContext.remove();
    }
}
