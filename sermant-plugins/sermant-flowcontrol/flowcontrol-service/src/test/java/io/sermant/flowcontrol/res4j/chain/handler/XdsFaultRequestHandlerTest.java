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


package io.sermant.flowcontrol.res4j.chain.handler;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsFlowControlService;
import io.sermant.core.service.xds.entity.*;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.config.XdsFlowControlConfig;
import io.sermant.flowcontrol.common.core.rule.fault.FaultException;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.res4j.chain.context.ChainContext;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/**
 * XdsFaultRequestHandler test
 *
 * @author zhp
 * @since 2025-04-12
 */
public class XdsFaultRequestHandlerTest {
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private XdsFlowControlService xdsFlowControlService;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        XdsFlowControlConfig xdsFlowControlConfig = new XdsFlowControlConfig();
        xdsFlowControlConfig.setEnable(true);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(XdsFlowControlConfig.class))
                .thenReturn(xdsFlowControlConfig);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        XdsCoreService xdsCoreService = Mockito.mock(XdsCoreService.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(XdsCoreService.class)).thenReturn(xdsCoreService);
        xdsFlowControlService = Mockito.mock(XdsFlowControlService.class);
        Mockito.when(xdsCoreService.getXdsFlowControlService()).thenReturn(xdsFlowControlService);
        XdsHttpFault xdsHttpFault = new XdsHttpFault();
        Mockito.when(xdsFlowControlService.getHttpFault(any(), any())).thenReturn(Optional.of(xdsHttpFault));
        XdsAbort xdsAbort = new XdsAbort();
        FractionalPercent fractionalPercent = new FractionalPercent();
        fractionalPercent.setNumerator(100);
        fractionalPercent.setDenominator(100);
        xdsAbort.setPercentage(fractionalPercent);
        xdsAbort.setHttpStatus(HttpStatus.SC_BAD_REQUEST);
        xdsHttpFault.setAbort(xdsAbort);
    }

    /**
     * test process
     */
    @Test
    public void test() {
        XdsFaultRequestHandler xdsFaultRequestHandler = new XdsFaultRequestHandler();
        FlowControlScenario scenarioInfo = new FlowControlScenario();
        scenarioInfo.setClusterName("outbound|8080||serviceA.default.svc.cluster.local");
        scenarioInfo.setRouteName("router");
        scenarioInfo.setServiceName("service");
        Assert.assertThrows(FaultException.class,
                () -> xdsFaultRequestHandler.onBefore(new HttpRequestEntity(), scenarioInfo));
    }

    @After
    public void clear() {
        pluginConfigManagerMockedStatic.close();
        ChainContext.remove();
    }
}
