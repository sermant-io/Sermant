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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.spring.cache.AppCache;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.Builder;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试EurekaHttpClientInterceptor
 *
 * @author provenceee
 * @since 2022-09-06
 */
public class EurekaHttpClientInterceptorTest {
    private final EurekaHttpClientInterceptor interceptor;

    private final RouterConfig routerConfig;

    private final ExecuteContext context;

    private static TestSpringConfigService configService;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
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

    public EurekaHttpClientInterceptorTest() throws IllegalAccessException, NoSuchFieldException {
        interceptor = new EurekaHttpClientInterceptor();
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
        Object[] arguments = new Object[1];
        Builder builder = Builder.newBuilder();
        builder.setAppName("foo");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "foo2");
        builder.setMetadata(metadata);
        arguments[0] = builder.build();
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    /**
     * 测试before方法
     */
    @Test
    public void testBefore() {
        interceptor.before(context);

        // ek会把服务名转为大写，所以这里期望值也是大写
        Assert.assertEquals("FOO", AppCache.INSTANCE.getAppName());
        InstanceInfo instanceInfo = (InstanceInfo) context.getArguments()[0];
        Map<String, String> metadata = instanceInfo.getMetadata();
        Assert.assertEquals(routerConfig.getRouterVersion(), metadata.get("version"));
        Assert.assertEquals("bar1", metadata.get("bar"));
        Assert.assertEquals("foo2", metadata.get("foo"));
    }

    /**
     * 测试after方法
     */
    @Test
    public void testAfter() {
        AppCache.INSTANCE.setAppName("FOO");
        interceptor.after(context);
        Assert.assertEquals(RouterConstant.SPRING_CACHE_NAME, configService.getCacheName());
        Assert.assertEquals("FOO", configService.getServiceName());
    }
}