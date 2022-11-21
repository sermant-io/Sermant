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

package com.huawei.discovery.interceptors.httpclient;

import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.PlugEffectStrategyCache;
import com.huawei.discovery.interceptors.httpclient.HttpClient4xInterceptorTest.Strategy;
import com.huawei.discovery.interceptors.httpclient.HttpClient4xInterceptorTest.TestInvokerService;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpAsyncUtils;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 异步httpclient测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class HttpAsyncClient4xInterceptorTest {
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;
    private MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;
    private MockedStatic<OperationManager> operationManagerMockedStatic;
    private final DiscoveryPluginConfig discoveryPluginConfig = new DiscoveryPluginConfig();
    private final String hostName = discoveryPluginConfig.getRealmName();
    private final String serviceName = "provider";
    private final String path = "/hello";
    private final String uri = "http://" + hostName + "/" + serviceName + path;
    private final HttpResponse commonResponse = Mockito.mock(HttpResponse.class);

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        pluginServiceManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class))
                .thenReturn(new DiscoveryPluginConfig());
        pluginServiceManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(new LbConfig());
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(InvokerService.class))
                .thenReturn(new TestInvokerService());
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
                .thenReturn(new Strategy());
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
        pluginServiceManagerMockedStatic.close();
        operationManagerMockedStatic.close();
    }

    @Test
    public void test() throws Exception {
        HttpAsyncUtils.remove();
        final HttpAsyncClient4xInterceptor interceptor = new HttpAsyncClient4xInterceptor();
        final ExecuteContext context = buildContext();
        PlugEffectStrategyCache.INSTANCE.resolve(DynamicConfigEventType.CREATE, "");
        interceptor.before(context);
        interceptor.onThrow(context);
        interceptor.after(context);

        //第二次调用, 存在结果
        context.changeResult(buildResult());
        interceptor.before(context);
        interceptor.onThrow(context);
        interceptor.after(context);
        final Future<HttpResponse> result = (Future<HttpResponse>) context.getResult();
        final HttpResponse response = result.get();
        Assert.assertEquals(response, commonResponse);
        result.get(1, TimeUnit.SECONDS);
        Assert.assertEquals(response, commonResponse);
    }

    private ExecuteContext buildContext() throws NoSuchMethodException, HttpException, IOException {
        final HttpAsyncRequestProducer producer = Mockito.mock(HttpAsyncRequestProducer.class);
        final HttpHost httpHost = Mockito.mock(HttpHost.class);
        Mockito.when(httpHost.getHostName()).thenReturn(hostName);
        final RequestLine requestLine = Mockito.mock(RequestLine.class);
        Mockito.when(requestLine.getMethod()).thenReturn("GET");
        Mockito.when(requestLine.getUri()).thenReturn(uri);
        final HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
        Mockito.when(httpRequest.getRequestLine()).thenReturn(requestLine);
        Mockito.when(producer.generateRequest()).thenReturn(httpRequest);
        Mockito.when(producer.getTarget()).thenReturn(httpHost);
        final FutureCallback callback = Mockito.mock(FutureCallback.class);
        final BasicAsyncResponseConsumer basicAsyncResponseConsumer = new BasicAsyncResponseConsumer();
        final Object[] args = {producer, basicAsyncResponseConsumer, null, callback};
        return ExecuteContext.forMemberMethod(this,
                this.getClass().getDeclaredMethod("execute", HttpAsyncRequestProducer.class,
                        HttpAsyncResponseConsumer.class, HttpContext.class, FutureCallback.class), args,
                null, null);
    }

    public Future<HttpResponse> execute(HttpAsyncRequestProducer producer,
            HttpAsyncResponseConsumer httpAsyncResponseConsumer, HttpContext context, FutureCallback callback) {
        return buildResult();
    }

    private Future<HttpResponse> buildResult() {
        return new Future<HttpResponse>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public HttpResponse get() {
                return commonResponse;
            }

            @Override
            public HttpResponse get(long timeout, TimeUnit unit) {
                return get();
            }
        };
    }
}
