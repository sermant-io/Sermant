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

package io.sermant.router.spring.interceptor;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.spring.TestServiceInstance;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Optional;

/**
 * @author daizhenyu
 * @since 2024-09-10
 **/
public class OkHttpClientInterceptorChainInterceptorTest {
    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private static MockedStatic<BaseHttpRouterUtils> mockedUtils;

    private static OkHttpClientInterceptorChainInterceptor interceptor;

    @BeforeClass
    public static void setUp() {
        RouterConfig routerConfig = new RouterConfig();
        routerConfig.setEnabledXdsRoute(true);
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RouterConfig.class))
                .thenReturn(routerConfig);
        mockedUtils = Mockito.mockStatic(BaseHttpRouterUtils.class);

        mockedUtils.when(() -> BaseHttpRouterUtils.isXdsRouteRequired(Mockito.any())).thenCallRealMethod();
        mockedUtils
                .when(() -> BaseHttpRouterUtils.rebuildUrlByXdsServiceInstance(Mockito.any(URI.class), Mockito.any()))
                .thenCallRealMethod();
        interceptor = new OkHttpClientInterceptorChainInterceptor();
    }

    @AfterClass
    public static void tearDown() {
        mockPluginConfigManager.close();
        mockedUtils.close();
    }

    @Test
    public void testBefore() throws Exception {
        Object obj = new Object();
        Object[] arguments = new Object[1];
        arguments[0] = new Request.Builder()
                .url("http://example.default.svc.cluster.local/test")
                .header("Header1", "Value1")
                .build();
        ExecuteContext context = ExecuteContext.forMemberMethod(obj, null, arguments, null, null);

        // service instance is null
        ExecuteContext result = interceptor.before(context);
        Request newRequest = (Request) result.getArguments()[0];
        Assert.assertNotNull(newRequest);
        HttpUrl newUrl = newRequest.httpUrl();
        Assert.assertEquals("http://example.default.svc.cluster.local/test", newUrl.toString());
        mockedUtils
                .when(() -> BaseHttpRouterUtils.chooseServiceInstanceByXds(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.ofNullable(null));

        // service instance is not empty
        TestServiceInstance serviceInstance = new TestServiceInstance();
        serviceInstance.setService("serviceA");
        serviceInstance.setHost("127.0.0.1");
        serviceInstance.setPort(8080);
        mockedUtils
                .when(() -> BaseHttpRouterUtils.chooseServiceInstanceByXds(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(serviceInstance));
        result = interceptor.before(context);
        newRequest = (Request) result.getArguments()[0];
        Assert.assertNotNull(newRequest);
        newUrl = newRequest.httpUrl();
        Assert.assertEquals("http://127.0.0.1:8080/test", newUrl.toString());
    }
}