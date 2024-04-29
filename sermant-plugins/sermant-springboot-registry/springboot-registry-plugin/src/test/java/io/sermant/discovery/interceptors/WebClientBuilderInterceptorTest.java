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

package io.sermant.discovery.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.config.LbConfig;
import io.sermant.discovery.entity.AbstractRetryExchangeFilterFunction;
import io.sermant.discovery.entity.RetryExchangeFilterFunction;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Test WebClientBuilderInterceptor
 *
 * @author provenceee
 * @since 2023-05-17
 */
public class WebClientBuilderInterceptorTest {
    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private final WebClientBuilderInterceptor interceptor;

    private final Method method;

    @BeforeClass
    public static void before() throws Exception {
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(new LbConfig());
    }

    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
    }

    public WebClientBuilderInterceptorTest() throws NoSuchMethodException {
        interceptor = new WebClientBuilderInterceptor();
        method = String.class.getDeclaredMethod("trim");
    }

    @Test
    public void testJettyClientHttpConnector() {
        Builder builder = WebClient.builder();
        ExecuteContext context = ExecuteContext.forMemberMethod(builder, method, null, null, null);
        builder.clientConnector(new JettyClientHttpConnector());
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());
    }

    @Test
    public void testReactorClientHttpConnector() {
        // Normal
        Builder builder = WebClient.builder();
        builder.clientConnector(new ReactorClientHttpConnector());
        ExecuteContext context = ExecuteContext.forMemberMethod(builder, method, null, null, null);
        interceptor.before(context);
        Assert.assertTrue(context.isSkip());
        WebClient client = (WebClient) context.getResult();
        Optional<Object> filters = ReflectUtils.getFieldValue(client.mutate(), "filters");
        Assert.assertTrue(filters.isPresent());
        Assert.assertEquals(1, ((List<?>) filters.get()).size());
        Assert.assertTrue(((List<?>) filters.get()).get(0) instanceof AbstractRetryExchangeFilterFunction);
        Assert.assertNull(ReflectUtils.getFieldValue(builder, "filters").orElse(null));

        // It has already been injected
        builder = WebClient.builder();
        context = ExecuteContext.forMemberMethod(builder, method, null, null, null);
        builder.filter(new RetryExchangeFilterFunction());
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());
    }
}