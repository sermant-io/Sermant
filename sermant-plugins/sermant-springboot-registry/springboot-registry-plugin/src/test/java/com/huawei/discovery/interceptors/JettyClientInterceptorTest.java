/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.interceptors;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.JettyClientWrapper;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpConversation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.URI;

/**
 * @author provenceee
 * @since 2023-05-17
 */
public class JettyClientInterceptorTest {
    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private static MockedStatic<PlugEffectWhiteBlackUtils> mockPlugEffectWhiteBlackUtils;

    private final JettyClientInterceptor interceptor;

    private final ExecuteContext context;

    private final Object[] arguments;

    @BeforeClass
    public static void before() throws Exception {
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(new LbConfig());

        mockPlugEffectWhiteBlackUtils = Mockito.mockStatic(PlugEffectWhiteBlackUtils.class);
        mockPlugEffectWhiteBlackUtils.when(() -> PlugEffectWhiteBlackUtils.isHostEqualRealmName("www.domain.com"))
                .thenReturn(true);
        mockPlugEffectWhiteBlackUtils.when(() -> PlugEffectWhiteBlackUtils.isPlugEffect("foo"))
                .thenReturn(true);
    }

    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
        mockPlugEffectWhiteBlackUtils.close();
    }

    public JettyClientInterceptorTest() throws NoSuchMethodException {
        interceptor = new JettyClientInterceptor();
        arguments = new Object[2];
        arguments[0] = new HttpConversation();
        context = ExecuteContext
                .forMemberMethod(Mockito.mock(HttpClient.class), String.class.getDeclaredMethod("trim"), arguments,
                        null, null);
    }

    @Test
    public void test() {
        // The domain name does not match
        arguments[1] = URI.create("http://www.domain1.com/foo/hello");
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // The name of the service does not match
        arguments[1] = URI.create("http://www.domain.com/bar/hello");
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        arguments[1] = URI.create("http://www.domain.com/foo/hello");
        interceptor.before(context);
        Assert.assertTrue(context.isSkip());
        Assert.assertTrue(context.getResult() instanceof JettyClientWrapper);
    }
}