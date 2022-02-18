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

package com.huawei.dubbo.register;

import com.huawei.dubbo.register.interceptor.ConfigValidationInterceptor;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.dubbo.common.URL;
import org.junit.Assert;
import org.junit.Test;

/**
 * 测试ConfigValidationInterceptor
 *
 * @author provenceee
 * @since 2022/2/14
 */
public class ConfigValidationInterceptorTest {
    private static final String REGISTRY_TYPE_KEY_1 = "registry-type";

    private static final String REGISTRY_TYPE_KEY_2 = "registry.type";

    private final ConfigValidationInterceptor interceptor;

    private final Object[] arguments;

    private final ExecuteContext context;

    public ConfigValidationInterceptorTest() {
        interceptor = new ConfigValidationInterceptor();
        arguments = new Object[1];
        context = ExecuteContext.forStaticMethod(null, null, arguments, null);
    }

    /**
     * 测试ConfigValidationUtils
     *
     * @see org.apache.dubbo.config.utils.ConfigValidationUtils
     */
    @Test
    public void testConfigValidationUtils() {
        // 测试非URL
        arguments[0] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertEquals(TestConstant.BAR, context.getArguments()[0]);

        // 测试非sc协议
        arguments[0] = URL.valueOf("dubbo://localhost:8080").addParameter(REGISTRY_TYPE_KEY_1, TestConstant.BAR);
        interceptor.before(context);
        Assert.assertEquals(TestConstant.BAR, ((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_1));

        // 测试sc协议parameters为null的情况
        arguments[0] = URL.valueOf(TestConstant.SC_ADDRESS);
        interceptor.before(context);
        Assert.assertEquals(0, ((URL) context.getArguments()[0]).getParameters().size());

        // 测试sc协议parameters有registry-type的情况
        arguments[0] = URL.valueOf(TestConstant.SC_ADDRESS).addParameter(REGISTRY_TYPE_KEY_1, TestConstant.BAR);
        interceptor.before(context);
        Assert.assertNull(((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_1));

        // 测试sc协议parameters有registry.type的情况
        arguments[0] = URL.valueOf(TestConstant.SC_ADDRESS).addParameter(REGISTRY_TYPE_KEY_2, TestConstant.BAR);
        interceptor.before(context);
        Assert.assertNull(((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_2));

        // 测试sc协议parameters有registry-type、registry.type的情况
        arguments[0] = URL.valueOf(TestConstant.SC_ADDRESS).addParameter(REGISTRY_TYPE_KEY_1, TestConstant.BAR)
            .addParameter(REGISTRY_TYPE_KEY_2, TestConstant.BAR);
        interceptor.before(context);
        Assert.assertTrue(((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_1) == null
            && ((URL) context.getArguments()[0]).getParameter(REGISTRY_TYPE_KEY_2) == null);
    }
}