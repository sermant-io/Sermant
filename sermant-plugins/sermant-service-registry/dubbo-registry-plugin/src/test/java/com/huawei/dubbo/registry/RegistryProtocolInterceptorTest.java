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

package com.huawei.dubbo.registry;

import com.huawei.dubbo.registry.interceptor.RegistryProtocolInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.registry.integration.InterfaceCompatibleRegistryProtocol;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Test RegistryProtocolInterceptor
 *
 * @author provenceee
 * @since 2022-02-15
 */
public class RegistryProtocolInterceptorTest {
    private static final int EXPECT_LENGTH = 3;

    private final RegistryProtocolInterceptor interceptor;

    private final Method method;

    private final InterfaceCompatibleRegistryProtocol protocol;

    /**
     * Constructor
     */
    public RegistryProtocolInterceptorTest() throws NoSuchMethodException {
        ApplicationModel.getConfigManager().setApplication(new ApplicationConfig(TestConstant.FOO));
        interceptor = new RegistryProtocolInterceptor();
        method = String.class.getMethod("trim");
        protocol = new InterfaceCompatibleRegistryProtocol();
    }

    /**
     * Test InterfaceCompatibleRegistryProtocol
     *
     * @see org.apache.dubbo.registry.integration.InterfaceCompatibleRegistryProtocol
     */
    @Test
    public void testInterfaceCompatibleRegistryProtocol() {
        // Test null
        ExecuteContext context = ExecuteContext.forMemberMethod(protocol, method, null, null, null);
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // The length of the test array is less than 3
        Object[] arguments = new Object[2];
        context = ExecuteContext.forMemberMethod(protocol, method, arguments, null, null);
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // Test arguments [2] are not URLs
        arguments = new Object[EXPECT_LENGTH];
        context = ExecuteContext.forMemberMethod(protocol, method, arguments, null, null);
        arguments[2] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // The protocol of the test URL is not sc
        arguments = new Object[EXPECT_LENGTH];
        context = ExecuteContext.forMemberMethod(protocol, method, arguments, null, null);
        arguments[2] = URL.valueOf("foo://localhost:8080");
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // The protocol for the test URL is sc
        arguments = new Object[EXPECT_LENGTH];
        context = ExecuteContext.forMemberMethod(protocol, method, arguments, null, null);
        arguments[2] = URL.valueOf(TestConstant.SC_ADDRESS);
        interceptor.before(context);
        Assert.assertTrue(context.isSkip());
    }
}