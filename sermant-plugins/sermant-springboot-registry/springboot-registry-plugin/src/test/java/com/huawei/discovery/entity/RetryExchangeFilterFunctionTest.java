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

package com.huawei.discovery.entity;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import reactor.core.publisher.Mono;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import java.net.URI;

/**
 * 测试RetryExchangeFilterFunction
 *
 * @author provenceee
 * @since 2023-05-17
 */
public class RetryExchangeFilterFunctionTest {
    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private static MockedStatic<PlugEffectWhiteBlackUtils> mockPlugEffectWhiteBlackUtils;

    private final RetryExchangeFilterFunction retryExchangeFilterFunction;

    private final LowerVersionRetryExchangeFilterFunction lowerVersionRetryExchangeFilterFunction;

    private final ExchangeFunction exchangeFunction;

    private final ClientRequest request;

    private final Mono<ClientResponse> response;

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

    public RetryExchangeFilterFunctionTest() {
        retryExchangeFilterFunction = new RetryExchangeFilterFunction();
        lowerVersionRetryExchangeFilterFunction = new LowerVersionRetryExchangeFilterFunction();
        exchangeFunction = Mockito.mock(ExchangeFunction.class);
        response = Mono.just(Mockito.mock(ClientResponse.class));
        Mockito.when(exchangeFunction.exchange(Mockito.any())).thenReturn(response);
        request = Mockito.mock(ClientRequest.class);
    }

    @Test
    public void testRetryExchangeFilterFunction() {
        test(retryExchangeFilterFunction);
    }

    @Test
    public void testLowerVersionRetryExchangeFilterFunction() {
        test(lowerVersionRetryExchangeFilterFunction);
    }

    private void test(AbstractRetryExchangeFilterFunction function) {
        // 域名不符
        Mockito.when(request.url()).thenReturn(URI.create("http://www.domain1.com/foo/hello"));
        Assert.assertEquals(response, function.filter(request, exchangeFunction));

        // 服务名不符
        Mockito.when(request.url()).thenReturn(URI.create("http://www.domain.com/bar/hello"));
        Assert.assertEquals(response, function.filter(request, exchangeFunction));

        // 织入重试
        Mockito.when(request.url()).thenReturn(URI.create("http://www.domain.com/foo/hello"));
        Assert.assertNotEquals(response, function.filter(request, exchangeFunction));
    }
}