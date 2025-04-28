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

package io.sermant.flowcontrol.retry.client;


import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsFlowControlService;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.entity.FlowControlResponse;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import io.sermant.flowcontrol.common.xds.circuit.XdsCircuitBreakerManager;
import io.sermant.flowcontrol.inject.ErrorCloseableHttpResponse;
import io.sermant.flowcontrol.service.rest4j.XdsHttpFlowControlService;
import okhttp3.*;
import okhttp3.internal.connection.RealCall;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * OkHttp3Interceptor test
 *
 * @author zhp
 * @since 2025-04-11
 */
public class OkHttp3InterceptorTest {
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private XdsHttpFlowControlService xdsHttpFlowControlService;

    private Request httpRequest;

    private Method method = this.getClass().getMethod("getResult", null);

    private XdsFlowControlService xdsFlowControlService;

    public OkHttp3InterceptorTest() throws NoSuchMethodException {
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        pluginServiceManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
    }

    /**
     * preinitialization
     *
     * @throws Exception initialization failure thrown
     */
    @Before
    public void before() throws Exception {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
        xdsHttpFlowControlService = Mockito.mock(XdsHttpFlowControlService.class);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(XdsHttpFlowControlService.class))
                .thenReturn(xdsHttpFlowControlService);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        XdsCoreService xdsCoreService = Mockito.mock(XdsCoreService.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(XdsCoreService.class)).thenReturn(xdsCoreService);
        xdsFlowControlService = Mockito.mock(XdsFlowControlService.class);
        Mockito.when(xdsCoreService.getXdsFlowControlService()).thenReturn(xdsFlowControlService);
        method = Mockito.mock(Method.class);
    }

    @Test
    public void test() throws Exception {
        OkHttp3ClientInterceptor interceptor = new OkHttp3ClientInterceptor();
        // test parameter type is incorrect
        ExecuteContext executeContext = buildErrorContext();
        interceptor.doBefore(executeContext);
        Assert.assertNull(executeContext.getResult());

        // test request URL does not meet the requirements
        executeContext = buildContext("http://localhost:8080/path");
        String resultMsg = "success";
        int code = HttpStatus.SC_BAD_REQUEST;
        AtomicBoolean triggeredFlag = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (triggeredFlag.get()) {
                Object[] args = invocation.getArguments();
                FlowControlResult flowControlResult = (FlowControlResult) args[1];
                flowControlResult.setSkip(true);
                flowControlResult.setResponse(new FlowControlResponse(resultMsg, code));
            }
            return null;
        }).when(xdsHttpFlowControlService).onBefore(any(), any());
        interceptor.doBefore(executeContext);
        Assert.assertNull(executeContext.getResult());

        // test triggered flow control rules, abort the request
        executeContext = buildContext("http://provider:8080/path");
        interceptor.doBefore(executeContext);
        Object result = executeContext.getResult();
        Assert.assertTrue(result instanceof Response);
        Response response = (Response) result;
        Assert.assertEquals(code, response.code());
        Assert.assertEquals(resultMsg, response.message());

        // test trigger circuit breaker rules
        triggeredFlag.set(false);
        XdsRequestCircuitBreakers xdsRequestCircuitBreakers = new XdsRequestCircuitBreakers();
        Mockito.when(xdsFlowControlService.getRequestCircuitBreakers(any(), any()))
                .thenReturn(Optional.of(xdsRequestCircuitBreakers));
        xdsRequestCircuitBreakers.setMaxRequests(1);
        FlowControlScenario scenario = new FlowControlScenario();
        scenario.setClusterName("test");
        scenario.setServiceName("provider");
        XdsCircuitBreakerManager.incrementActiveRequests(scenario.getServiceName(), scenario.getClusterName());
        XdsThreadLocalUtil.setScenarioInfo(scenario);
        interceptor.doBefore(executeContext);
        result = executeContext.getResult();
        Assert.assertTrue(result instanceof Response);
        response = (Response) result;
        Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.code());
        Assert.assertEquals("CircuitBreaker has forced open and deny any requests",
                response.message());
    }

    private ExecuteContext buildContext(String url) {
        httpRequest = new okhttp3.Request.Builder().url(url).build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(httpRequest);
        return ExecuteContext.forMemberMethod(call, method, new Object[]{null, httpRequest}, null, null);
    }

    private ExecuteContext buildErrorContext() {
        Call call = Mockito.mock(Call.class);
        return ExecuteContext.forMemberMethod(call, method, new Object[]{null, ""}, null, null);
    }

    public String getResult(){
        return "success";
    }
}
