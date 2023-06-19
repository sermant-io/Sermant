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

import com.huawei.discovery.TestInvokerService;
import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.entity.SimpleRequestRecorder;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.AbstractClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * 测试HttpComponentsClientHttpConnectorInterceptor
 *
 * @author provenceee
 * @since 2023-05-17
 */
public class HttpComponentsClientHttpConnectorInterceptorTest {
    private static final URI HELLO_URI = URI.create("http://www.domain.com/bar/hello?name=foo");

    private static final URI THROW_URI = URI.create("http://www.domain.com/bar/throw?name=foo");

    private static final String INSTANCE_URL = "http://127.0.0.1:8010/hello?name=foo";

    private static MockedStatic<ServiceManager> mockServiceManager;

    private static MockedStatic<PlugEffectWhiteBlackUtils> mockPlugEffectWhiteBlackUtils;

    private final HttpComponentsClientHttpConnectorInterceptor interceptor;

    private final Object[] arguments;

    private final java.lang.reflect.Method method;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(InvokerService.class))
                .thenReturn(new TestInvokerService());

        mockPlugEffectWhiteBlackUtils = Mockito.mockStatic(PlugEffectWhiteBlackUtils.class);
        mockPlugEffectWhiteBlackUtils.when(() -> PlugEffectWhiteBlackUtils.isAllowRun("www.domain.com", "bar"))
                .thenReturn(true);

        Optional<Object> recorder = ReflectUtils.getStaticFieldValue(RequestInterceptorUtils.class, "RECORDER");
        if (recorder.isPresent()) {
            SimpleRequestRecorder simpleRequestRecorder = (SimpleRequestRecorder) recorder.get();
            Optional<Object> config = ReflectUtils.getFieldValue(simpleRequestRecorder, "discoveryPluginConfig");
            if (config.isPresent()) {
                return;
            }
            Field field = simpleRequestRecorder.getClass().getDeclaredField("discoveryPluginConfig");
            field.setAccessible(true);
            field.set(simpleRequestRecorder, new DiscoveryPluginConfig());
        }
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
        mockPlugEffectWhiteBlackUtils.close();
    }

    public HttpComponentsClientHttpConnectorInterceptorTest() throws NoSuchMethodException {
        arguments = new Object[2];
        arguments[1] = HttpClientContext.create();
        method = getClass().getDeclaredMethod("testMethod",
                HttpComponentsClientHttpRequest.class, HttpClientContext.class);
        interceptor = new HttpComponentsClientHttpConnectorInterceptor();
    }

    @Test
    public void test() {
        // 测试正常情况
        AbstractClientHttpRequest request = new HttpComponentsClientHttpRequest(HttpMethod.GET, HELLO_URI,
                HttpClientContext.create(), new DefaultDataBufferFactory());
        arguments[0] = request;
        ReflectUtils.setFieldValue(request, "httpRequest", new BasicHttpRequest(Method.GET, HELLO_URI));
        ExecuteContext context = ExecuteContext.forMemberMethod(this, method, arguments, null, null);
        interceptor.doBefore(context);
        request = (AbstractClientHttpRequest) context.getArguments()[0];
        Assert.assertEquals(INSTANCE_URL, request.getURI().toString());
    }

    @Test
    public void testException() {
        // 测试异常情况
        AbstractClientHttpRequest request = new HttpComponentsClientHttpRequest(HttpMethod.GET, THROW_URI,
                HttpClientContext.create(), new DefaultDataBufferFactory());
        arguments[0] = request;
        ReflectUtils.setFieldValue(request, "httpRequest", new BasicHttpRequest(Method.GET, THROW_URI));
        ExecuteContext context = ExecuteContext.forMemberMethod(this, method, arguments, null, null);
        interceptor.doBefore(context);
        Assert.assertEquals(ConnectException.class, context.getThrowableOut().getClass());
    }

    public Mono<ClientHttpResponse> testMethod(HttpComponentsClientHttpRequest request, HttpClientContext context)
            throws ConnectException {
        if (request.getURI().getPath().contains("throw")) {
            throw new ConnectException();
        }
        return Mono.empty();
    }

    public static class HttpComponentsClientHttpRequest extends AbstractClientHttpRequest {
        private final HttpRequest httpRequest;

        private final DataBufferFactory dataBufferFactory;

        private final HttpClientContext context;

        private final URI uri;

        @Nullable
        private Flux<ByteBuffer> byteBufferFlux;

        public HttpComponentsClientHttpRequest(HttpMethod method, URI uri, HttpClientContext context,
                DataBufferFactory dataBufferFactory) {
            this.context = context;
            this.httpRequest = new BasicHttpRequest(method.name(), uri);
            this.uri = uri;
            this.dataBufferFactory = dataBufferFactory;
        }

        @Override
        protected void applyHeaders() {

        }

        @Override
        protected void applyCookies() {

        }

        @Override
        public HttpMethod getMethod() {
            return HttpMethod.GET;
        }

        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public DataBufferFactory bufferFactory() {
            return dataBufferFactory;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> publisher) {
            return null;
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> publisher) {
            return null;
        }

        @Override
        public Mono<Void> setComplete() {
            return null;
        }
    }
}