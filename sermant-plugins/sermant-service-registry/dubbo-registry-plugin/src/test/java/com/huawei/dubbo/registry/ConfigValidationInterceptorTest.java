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

package com.huawei.dubbo.registry;

import com.huawei.dubbo.registry.interceptor.ConfigValidationInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.dubbo.common.URL;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test ConfigValidationInterceptor
 *
 * @author provenceee
 * @since 2022-02-14
 */
public class ConfigValidationInterceptorTest {
    private static final String REGISTRY_TYPE_KEY_1 = "registry-type";

    private static final String REGISTRY_TYPE_KEY_2 = "registry.type";

    private final ConfigValidationInterceptor interceptor;

    private final Object[] arguments;

    private final ExecuteContext context;

    /**
     * Constructor
     */
    public ConfigValidationInterceptorTest() {
        interceptor = new ConfigValidationInterceptor();
        arguments = new Object[1];
        context = ExecuteContext.forStaticMethod(null, null, arguments, null);
    }

    /**
     * Test ConfigValidationUtils
     *
     * @see org.apache.dubbo.config.utils.ConfigValidationUtils
     */
    @Test
    public void testConfigValidationUtils() {
        // Test non-URLs
        arguments[0] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertEquals(TestConstant.BAR, context.getArguments()[0]);

        // Testing non SC protocols
        arguments[0] = URL.valueOf("dubbo://localhost:8080").addParameter(REGISTRY_TYPE_KEY_1, TestConstant.BAR);
        interceptor.before(context);
        Assert.assertEquals(TestConstant.BAR, ((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_1));

        // Test if the parameters of the SC protocol are null
        arguments[0] = URL.valueOf(TestConstant.SC_ADDRESS);
        interceptor.before(context);
        Assert.assertEquals(0, ((URL) context.getArguments()[0]).getParameters().size());

        // Testing the presence of registry type in the parameters of the SC protocol
        arguments[0] = URL.valueOf(TestConstant.SC_ADDRESS).addParameter(REGISTRY_TYPE_KEY_1, TestConstant.BAR);
        interceptor.before(context);
        Assert.assertNull(((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_1));

        // Testing the presence of registry.type in the parameters of the SC protocol
        arguments[0] = URL.valueOf(TestConstant.SC_ADDRESS).addParameter(REGISTRY_TYPE_KEY_2, TestConstant.BAR);
        interceptor.before(context);
        Assert.assertNull(((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_2));

        // Test whether the parameters of the SC protocol have a registry type and registry.type
        arguments[0] = URL.valueOf(TestConstant.SC_ADDRESS).addParameter(REGISTRY_TYPE_KEY_1, TestConstant.BAR)
            .addParameter(REGISTRY_TYPE_KEY_2, TestConstant.BAR);
        interceptor.before(context);
        Assert.assertTrue(((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_1) == null
            && ((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_2) == null);
    }
}
