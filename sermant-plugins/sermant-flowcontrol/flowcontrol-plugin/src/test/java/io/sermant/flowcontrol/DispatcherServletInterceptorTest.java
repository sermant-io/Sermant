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

package io.sermant.flowcontrol;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.flowcontrol.common.config.ConfigConst;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.service.rest4j.HttpRest4jService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * test http request interceptor
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class DispatcherServletInterceptorTest {
    private final FlowControlConfig flowControlConfig = new FlowControlConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
    }

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(flowControlConfig);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(HttpRest4jService.class))
                .thenReturn(createRestService());
    }

    @Test
    public void test() throws Exception {
        final DispatcherServletInterceptor interceptor = new DispatcherServletInterceptor();
        final ExecuteContext executeContext = buildContext();
        interceptor.doBefore(executeContext);
        Assert.assertTrue(executeContext.isSkip());
        interceptor.onThrow(executeContext);
        interceptor.doThrow(executeContext);
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn("/api");
        Mockito.when(request.getRequestURI()).thenReturn("/context");
        Mockito.when(request.getMethod()).thenReturn("POST");
        Mockito.when(request.getHeaderNames()).thenReturn(new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return null;
            }
        });
        Mockito.when(request.getHeader(ConfigConst.FLOW_REMOTE_SERVICE_NAME_HEADER_KEY)).thenReturn("serviceName");
        return ExecuteContext.forMemberMethod(this, String.class.getMethod("trim"), new Object[] {request, null},
                Collections.emptyMap(), Collections.emptyMap());
    }

    private HttpRest4jService createRestService() {
        return new HttpRest4jService() {
            @Override
            public void onBefore(String sourceName, RequestEntity requestEntity,
                    FlowControlResult fixedResult) {
                fixedResult.setSkip(true);
            }

            @Override
            public void onAfter(String sourceName, Object result) {

            }

            @Override
            public void onThrow(String sourceName, Throwable throwable) {

            }
        };
    }
}
