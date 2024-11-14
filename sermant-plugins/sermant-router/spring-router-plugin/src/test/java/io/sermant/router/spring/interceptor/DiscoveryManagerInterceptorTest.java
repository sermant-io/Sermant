/*
 * Copyright (C) 2022-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.spring.TestSpringConfigService;
import io.sermant.router.spring.cache.AppCache;
import io.sermant.router.spring.service.SpringConfigService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Test DiscoveryManagerInterceptor
 *
 * @author provenceee
 * @since 2022-10-13
 */
public class DiscoveryManagerInterceptorTest {
    private final DiscoveryManagerInterceptor interceptor;

    private final ExecuteContext context;

    private static TestSpringConfigService configService;

    private static MockedStatic<ServiceManager> mockServiceManager;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private static RouterConfig routerConfig;

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void before() {
        configService = new TestSpringConfigService();
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class)).thenReturn(configService);

        routerConfig = new RouterConfig();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("foo", "foo1");
        parameters.put("bar", "bar1");
        routerConfig.setParameters(parameters);
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RouterConfig.class))
                .thenReturn(routerConfig);
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
        mockPluginConfigManager.close();
    }

    public DiscoveryManagerInterceptorTest() {
        interceptor = new DiscoveryManagerInterceptor();
        Object[] arguments = new Object[1];
        arguments[0] = new TestObject("foo");
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    /**
     * Test the before method
     */
    @Test
    public void testBefore() {
        interceptor.before(context);
        Assert.assertEquals("foo", AppCache.INSTANCE.getAppName());
        Map<String, String> metadata = ((TestObject) context.getArguments()[0]).getMetadata();
        Assert.assertEquals("bar1", metadata.get("bar"));
        Assert.assertEquals("foo1", metadata.get("foo"));
        Assert.assertEquals(routerConfig.getRouterVersion(), metadata.get("version"));

        context.getArguments()[0] = new TestObject(null);
        interceptor.before(context);
        Assert.assertEquals("foo", AppCache.INSTANCE.getAppName());
        Assert.assertEquals(RouterConstant.SPRING_CACHE_NAME, configService.getCacheName());
        Assert.assertEquals("foo", configService.getServiceName());
    }

    public static class TestObject {
        private final String serviceName;

        private final Map<String, String> metadata;

        /**
         * Constructor
         *
         * @param serviceName Service name
         */
        public TestObject(String serviceName) {
            this.serviceName = serviceName;
            this.metadata = new HashMap<>();
        }

        public String getServiceName() {
            return serviceName;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
}