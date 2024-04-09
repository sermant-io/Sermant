/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;

import com.alibaba.dubbo.config.ApplicationConfig;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Test ApplicationConfigInterceptor
 *
 * @author provenceee
 * @since 2022-09-28
 */
public class ApplicationConfigInterceptorTest {
    private final RouterConfig config;

    private final ApplicationConfigInterceptor interceptor;

    /**
     * Constructor
     */
    public ApplicationConfigInterceptorTest() throws IllegalAccessException, NoSuchFieldException {
        interceptor = new ApplicationConfigInterceptor();
        config = new RouterConfig();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("foo", "foo1");
        config.setParameters(parameters);
        config.setRouterVersion("1.0.0");
        config.setZone("bar");
        Field field = interceptor.getClass().getDeclaredField("routerConfig");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(interceptor, config);
    }

    /**
     * Test setName method
     */
    @Test
    public void testSetName() throws NoSuchMethodException {
        Object[] args = new Object[1];
        args[0] = "";
        ApplicationConfig applicationConfig = new ApplicationConfig();
        ExecuteContext context = ExecuteContext.forMemberMethod(applicationConfig,
                ApplicationConfig.class.getMethod("setName", String.class), args, null, null);

        // the app name is empty
        interceptor.before(context);
        Assert.assertNull(DubboCache.INSTANCE.getAppName());
        Assert.assertNull(applicationConfig.getParameters());

        // the app name is not empty
        args[0] = "foo";
        interceptor.before(context);
        Assert.assertEquals("foo", DubboCache.INSTANCE.getAppName());
        Map<String, String> parameters = applicationConfig.getParameters();
        Assert.assertNotNull(parameters);
        Assert.assertEquals(0, parameters.size());
    }

    /**
     * Test the putParameters method
     */
    @Test
    public void testPutParametersWithNull() throws NoSuchMethodException {
        Object[] args = new Object[1];
        args[0] = null;
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(),
                ApplicationConfig.class.getMethod("setParameters", Map.class), args, null, null);

        // map is null
        interceptor.before(context);
        Map<String, String> parameters = (Map<String, String>) context.getArguments()[0];
        Assert.assertEquals(config.getParameters().size() + 2, parameters.size());
        Assert.assertEquals(config.getRouterVersion(), parameters.get(RouterConstant.META_VERSION_KEY));
        Assert.assertEquals(config.getZone(), parameters.get(RouterConstant.META_ZONE_KEY));
        Map<String, String> configParameters = config.getParameters();
        configParameters.forEach(
                (key, value) -> Assert.assertEquals(value, parameters.get(RouterConstant.PARAMETERS_KEY_PREFIX + key)));
    }

    /**
     * Test the putParameters method
     */
    @Test
    public void testPutParametersWithNotEmpty() throws NoSuchMethodException {
        // map is not empty
        Map<String, String> map = new HashMap<>();
        map.put("bar", "bar1");
        Object[] args = new Object[1];
        args[0] = map;
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(),
                ApplicationConfig.class.getMethod("setParameters", Map.class), args, null, null);
        interceptor.before(context);
        Map<String, String> parameters = (Map<String, String>) context.getArguments()[0];
        Assert.assertEquals(config.getParameters().size() + 3, parameters.size());
        Assert.assertEquals(config.getRouterVersion(), parameters.get(RouterConstant.META_VERSION_KEY));
        Assert.assertEquals(config.getZone(), parameters.get(RouterConstant.META_ZONE_KEY));
        Map<String, String> configParameters = config.getParameters();
        configParameters.forEach(
                (key, value) -> Assert.assertEquals(value, parameters.get(RouterConstant.PARAMETERS_KEY_PREFIX + key)));
        map.forEach(
                (key, value) -> Assert.assertEquals(value, parameters.get(key)));
    }
}