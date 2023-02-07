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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.router.common.config.RouterConfig;

/**
 * 测试AbstractConfigInterceptor
 *
 * @author chengyouling
 * @since 2022-12-28
 */
public class AbstractConfigInterceptorTest {

    private final AbstractConfigInterceptor interceptor;

    /**
     * 构造方法
     */
    public AbstractConfigInterceptorTest() throws IllegalAccessException, NoSuchFieldException {
        interceptor = new AbstractConfigInterceptor();
        RouterConfig config = new RouterConfig();
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
     * 测试putParameters方法
     */
    @Test
    public void testBefore() throws NoSuchMethodException {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        Map<String, String> map = new HashMap<>();
        Object[] arguments = new Object[2];
        arguments[0] = map;
        arguments[1] = applicationConfig;
        Method method = AbstractConfig.class.getDeclaredMethod("appendParameters", Map.class, Object.class);
        method.setAccessible(true);
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), method, arguments, null, null);
        interceptor.before(context);
        Map<String, String> parameters = (Map<String, String>) context.getArguments()[0];
        Assert.assertEquals(3, parameters.size());
    }
}