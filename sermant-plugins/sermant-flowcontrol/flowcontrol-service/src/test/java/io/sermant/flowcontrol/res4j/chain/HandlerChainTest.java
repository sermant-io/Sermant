/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.flowcontrol.res4j.chain;

import static org.junit.Assert.assertEquals;

import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.flowcontrol.common.config.XdsFlowControlConfig;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity.Builder;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.res4j.chain.context.ChainContext;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;
import io.sermant.flowcontrol.res4j.chain.handler.BulkheadRequestHandler;
import io.sermant.flowcontrol.res4j.chain.handler.CircuitBreakerRequestHandler;
import io.sermant.flowcontrol.res4j.chain.handler.FaultRequestHandler;
import io.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Set;

/**
 * tests for handler chains
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class HandlerChainTest {
    private MockedStatic<OperationManager> operationManagerMockedStatic;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
                .thenReturn(new YamlConverterImpl());
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(XdsFlowControlConfig.class))
                .thenReturn(new XdsFlowControlConfig());
    }

    // The mock static method needs to be closed when it is finished
    @After
    public void tearDown() {
        operationManagerMockedStatic.close();
        pluginConfigManagerMockedStatic.close();
    }

    /**
     * test link concatenation and invocation
     */
    @Test
    public void testChain() {
        final HandlerChain handlerChain = new HandlerChain();
        final BulkheadRequestHandler bulkheadRequestHandler = Mockito.spy(BulkheadRequestHandler.class);
        final CircuitBreakerRequestHandler circuitBreakerClientReqHandler =
                Mockito.spy(CircuitBreakerRequestHandler.class);
        final FaultRequestHandler faultRequestHandler = Mockito.spy(FaultRequestHandler.class);
        handlerChain.addLastHandler(bulkheadRequestHandler);
        handlerChain.addLastHandler(circuitBreakerClientReqHandler);
        handlerChain.addLastHandler(faultRequestHandler);
        int num = 3;
        AbstractChainHandler handler = handlerChain;
        while (handler.getNext() != null) {
            num--;
            handler = handler.getNext();
        }
        assertEquals(0, num);

        // test call
        final RequestContext requestContext = ChainContext.getThreadLocalContext("test");
        final HttpRequestEntity build = new Builder().setRequestType(RequestType.CLIENT).setApiPath("/api").build();
        requestContext.setRequestEntity(build);
        FlowControlScenario scenarioInfo = new FlowControlScenario();
        final Set<String> businessNames = Collections.singleton("test");
        final Exception exception = new IllegalArgumentException("error");
        final Object result = new Object();
        scenarioInfo.setMatchedScenarioNames(businessNames);
        handlerChain.onBefore(requestContext, scenarioInfo);
        handlerChain.onThrow(requestContext, scenarioInfo, exception);
        handlerChain.onResult(requestContext, scenarioInfo, result);
        Mockito.verify(bulkheadRequestHandler, Mockito.times(1))
                .onBefore(requestContext, scenarioInfo);
        Mockito.verify(bulkheadRequestHandler, Mockito.times(1))
                .onThrow(requestContext, scenarioInfo, exception);
        Mockito.verify(bulkheadRequestHandler, Mockito.times(1))
                .onResult(requestContext, scenarioInfo, result);

        Mockito.verify(circuitBreakerClientReqHandler, Mockito.times(1))
                .onBefore(requestContext, scenarioInfo);
        Mockito.verify(circuitBreakerClientReqHandler, Mockito.times(1))
                .onThrow(requestContext, scenarioInfo, exception);
        Mockito.verify(circuitBreakerClientReqHandler, Mockito.times(1))
                .onResult(requestContext, scenarioInfo, result);

        Mockito.verify(faultRequestHandler, Mockito.times(1))
                .onBefore(requestContext, scenarioInfo);
        Mockito.verify(faultRequestHandler, Mockito.times(1))
                .onThrow(requestContext, scenarioInfo, exception);
        Mockito.verify(faultRequestHandler, Mockito.times(1))
                .onResult(requestContext, scenarioInfo, result);

    }
}
