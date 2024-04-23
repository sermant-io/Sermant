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

package io.sermant.dubbo.registry;

import com.alibaba.dubbo.common.URL;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.dubbo.registry.interceptor.AlibabaInterfaceConfigInterceptor;
import io.sermant.registry.config.RegisterConfig;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test AlibabaInterfaceConfigInterceptor
 *
 * @author provenceee
 * @since 2022-11-25
 */
public class AlibabaInterfaceConfigInterceptorTest {
    private final AlibabaInterfaceConfigInterceptor interceptor;

    private final RegisterConfig registerConfig;

    private final ExecuteContext context;

    private final List<URL> urls;

    public AlibabaInterfaceConfigInterceptorTest()
        throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
        urls = new ArrayList<>();
        urls.add(new URL("registry", "127.0.0.1", 80).addParameter("registry", "foo"));
        interceptor = new AlibabaInterfaceConfigInterceptor();
        registerConfig = new RegisterConfig();
        Field field = interceptor.getClass().getDeclaredField("config");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(interceptor, registerConfig);
        context = ExecuteContext.forMemberMethod(new Object(), String.class.getMethod("trim"), new Object[0], null,
            null);
    }

    /**
     * Initialize
     */
    @Before
    public void init() {
        registerConfig.setEnableDubboRegister(false);
        registerConfig.setOpenMigration(false);
        context.changeResult(urls);
    }

    /**
     * Test when the switch is off
     */
    @Test
    public void testDisabled() {
        interceptor.after(context);
        Assert.assertEquals(urls, context.getResult());
        Assert.assertEquals(1, ((List<URL>) context.getResult()).size());
    }

    /**
     * Test when the switch is off
     */
    @Test
    public void testEnableDubboRegister() {
        registerConfig.setEnableDubboRegister(true);

        // 测试空list
        context.changeResult(Collections.emptyList());
        interceptor.after(context);
        Assert.assertEquals(Collections.emptyList(), context.getResult());

        // 测试合法数据
        context.changeResult(urls);
        interceptor.after(context);
        List<URL> result = (List<URL>) context.getResult();
        Assert.assertNotEquals(urls, result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("registry", result.get(0).getProtocol());
        Assert.assertEquals("sc", result.get(0).getParameter("registry"));
    }

    /**
     * Test when the switch is off
     */
    @Test
    public void testOpenMigration() {
        registerConfig.setEnableDubboRegister(true);
        registerConfig.setOpenMigration(true);

        // 测试空list
        context.changeResult(Collections.emptyList());
        interceptor.after(context);
        Assert.assertEquals(Collections.emptyList(), context.getResult());

        // 测试合法数据
        context.changeResult(urls);
        interceptor.after(context);
        List<URL> result = (List<URL>) context.getResult();
        Assert.assertEquals(urls, result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("registry", result.get(1).getProtocol());
        Assert.assertEquals("sc", result.get(1).getParameter("registry"));
        urls.remove(1);
    }
}