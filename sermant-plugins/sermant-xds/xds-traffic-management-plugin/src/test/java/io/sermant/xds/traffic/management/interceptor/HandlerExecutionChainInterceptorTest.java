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

package io.sermant.xds.traffic.management.interceptor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsSecurityService;
import io.sermant.core.utils.ClassUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;

@RunWith(MockitoJUnitRunner.class)
public class HandlerExecutionChainInterceptorTest {
    private HandlerExecutionChainInterceptor interceptor;

    private ExecuteContext executeContext;

    private HandlerExecutionChain handlerExecutionChain;

    private XdsCoreService xdsCoreService;

    private XdsSecurityService xdsSecurityService;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    @Before
    public void setUp() {
        xdsCoreService = mock(XdsCoreService.class);
        xdsSecurityService = mock(XdsSecurityService.class);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(XdsCoreService.class))
                .thenReturn(xdsCoreService);
        when(xdsCoreService.getXdsSecurityService()).thenReturn(xdsSecurityService);
        interceptor = new HandlerExecutionChainInterceptor();
        executeContext = ExecuteContext.forMemberMethod(new HandlerExecutionChain(new Object()
        ), null, null, null, null);
    }

    @After
    public void tearDown() {
        serviceManagerMockedStatic.close();
    }

    @Test
    public void getInterceptor_FirstCall_ShouldCreateNewInstance() {
        try (MockedStatic<ClassUtils> mockedStatic = mockStatic(ClassUtils.class)) {
            HandlerInterceptor result = interceptor.getInterceptor();
            assertNotNull(result);
            assertTrue(result instanceof HandlerInterceptor);
            mockedStatic.verify(() -> ClassUtils.defineClass(
                    eq("io.sermant.xds.traffic.management.interceptor.AuthHandlerInterceptor"),
                    any()));
        }
    }

    @Test
    public void after_ShouldReturnSameContext() {
        ExecuteContext result = interceptor.after(executeContext);
        assertSame(executeContext, result);
    }
}
