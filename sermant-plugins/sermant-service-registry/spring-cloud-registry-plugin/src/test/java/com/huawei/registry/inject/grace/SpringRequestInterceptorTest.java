/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.inject.grace;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;
import com.huawei.registry.services.GraceService;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor test
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class SpringRequestInterceptorTest {
    private final String testAddress = "localhost:8099";

    private final List<String> addresses = new ArrayList<>();

    private final GraceConfig graceConfig = new GraceConfig();

    private final GraceService graceService = new GraceService() {
        @Override
        public void shutdown() {

        }

        @Override
        public void addAddress(String address) {
            addresses.add(address);
        }
    };

    private SpringRequestInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(graceConfig);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(GraceService.class))
                .thenReturn(graceService);
        graceConfig.setEnableGraceShutdown(true);
        graceConfig.setEnableOfflineNotify(true);
        graceConfig.setEnableSpring(true);
        Mockito.when(request.getHeader(GraceConstants.GRACE_OFFLINE_SOURCE_KEY))
                .thenReturn(GraceConstants.GRACE_OFFLINE_SOURCE_VALUE);
        Mockito.when(request.getHeader(GraceConstants.SERMANT_GRACE_ADDRESS))
                .thenReturn(testAddress);
        interceptor = new SpringRequestInterceptor();
    }

    @After
    public void tearDown() throws Exception {
        pluginServiceManagerMockedStatic.close();
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void preHandle() {
        GraceContext.INSTANCE.getGraceShutDownManager().setShutDown(true);
        interceptor.preHandle(request, response, new Object());
        Assert.assertTrue(GraceContext.INSTANCE.getStartWarmUpTime() > 0);
        Assert.assertTrue(addresses.contains(testAddress));
        Assert.assertTrue(GraceContext.INSTANCE.getGraceShutDownManager().getRequestCount() > 0);
        Mockito.verify(response, Mockito.atLeastOnce()).addHeader(Mockito.anyString(), Mockito.anyString());
        GraceContext.INSTANCE.getGraceShutDownManager().setShutDown(false);
        GraceContext.INSTANCE.setStartWarmUpTime(0);
        GraceContext.INSTANCE.getGraceShutDownManager().decreaseRequestCount();
    }

    @Test
    public void postHandle() {
        interceptor.postHandle(request, response, new Object(), null);
    }

    @Test
    public void afterCompletion() {
        interceptor.afterCompletion(request, response, new Object(), null);
        Assert.assertTrue(GraceContext.INSTANCE.getGraceShutDownManager().getRequestCount() < 0);
        GraceContext.INSTANCE.getGraceShutDownManager().increaseRequestCount();
    }
}
