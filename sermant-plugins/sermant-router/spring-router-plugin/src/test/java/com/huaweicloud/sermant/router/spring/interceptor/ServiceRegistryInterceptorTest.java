/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.spring.cache.AppCache;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试ServiceRegistryInterceptor
 *
 * @author provenceee
 * @since 2022-09-06
 */
public class ServiceRegistryInterceptorTest {
    private final ServiceRegistryInterceptor interceptor;

    private final RouterConfig routerConfig;

    private final ExecuteContext context;

    private static TestSpringConfigService configService;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstant.LOG_SETTING_FILE_KEY,
            ServiceRegistryInterceptorTest.class.getResource("/logback-test.xml").getPath());
        LoggerFactory.init(map);
        configService = new TestSpringConfigService();
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class)).thenReturn(configService);
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public ServiceRegistryInterceptorTest() throws IllegalAccessException, NoSuchFieldException {
        interceptor = new ServiceRegistryInterceptor();
        routerConfig = new RouterConfig();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("foo", "foo1");
        parameters.put("bar", "bar1");
        routerConfig.setParameters(parameters);
        Field field = interceptor.getClass().getDeclaredField("routerConfig");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(interceptor, routerConfig);
        TestRegistration registration = new TestRegistration();
        registration.getMetadata().put("foo", "foo2");
        TestServiceRegistration serviceRegistration = new TestServiceRegistration(registration);
        context = ExecuteContext.forMemberMethod(serviceRegistration, null, null, null, null);
    }

    @Test
    public void testBefore() {
        interceptor.before(context);
        Assert.assertEquals("foo", AppCache.INSTANCE.getAppName());
        TestServiceRegistration registration = (TestServiceRegistration) context.getObject();
        Map<String, String> metadata = registration.getRegistration().getMetadata();
        Assert.assertEquals(routerConfig.getRouterVersion(), metadata.get("version"));
        Assert.assertEquals("bar1", metadata.get("bar"));
        Assert.assertEquals("foo2", metadata.get("foo"));
    }

    @Test
    public void testAfter() {
        AppCache.INSTANCE.setAppName("foo");
        interceptor.after(context);
        Assert.assertEquals(RouterConstant.SPRING_CACHE_NAME, configService.getCacheName());
        Assert.assertEquals("foo", configService.getServiceName());
    }

    public static class TestServiceRegistration extends AbstractAutoServiceRegistration<TestRegistration> {
        private final TestRegistration registration;

        protected TestServiceRegistration(TestRegistration registration) {
            super(null, null);
            this.registration = registration;
        }

        @Override
        protected Object getConfiguration() {
            return null;
        }

        @Override
        protected boolean isEnabled() {
            return false;
        }

        @Override
        protected TestRegistration getRegistration() {
            return registration;
        }

        @Override
        protected TestRegistration getManagementRegistration() {
            return null;
        }
    }

    public static class TestRegistration implements Registration {
        private final Map<String, String> metadata = new HashMap<>();

        @Override
        public String getServiceId() {
            return "foo";
        }

        @Override
        public String getHost() {
            return null;
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
}