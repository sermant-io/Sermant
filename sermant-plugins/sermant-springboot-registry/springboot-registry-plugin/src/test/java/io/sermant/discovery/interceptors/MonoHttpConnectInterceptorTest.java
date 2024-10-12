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
import io.sermant.core.service.ServiceManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.TestInvokerService;
import io.sermant.discovery.config.DiscoveryPluginConfig;
import io.sermant.discovery.entity.SimpleRequestRecorder;
import io.sermant.discovery.service.InvokerService;
import io.sermant.discovery.utils.PlugEffectWhiteBlackUtils;
import io.sermant.discovery.utils.RequestInterceptorUtils;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.reactivestreams.Subscription;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * ExchangeFunctionInterceptor Test
 *
 * @author provenceee
 * @since 2023-04-27
 */
public class MonoHttpConnectInterceptorTest {
    private static final String HOST_URL = "http://www.domain.com/bar/hello?name=foo";

    private static final String INSTANCE_URL = "http://127.0.0.1:8010/hello?name=foo";

    private static MockedStatic<ServiceManager> mockServiceManager;

    private static MockedStatic<PlugEffectWhiteBlackUtils> mockPlugEffectWhiteBlackUtils;

    private final MonoHttpConnectInterceptor interceptor;

    private final Mono<?> monoHttpConnect;

    private final Object configuration;

    private final ExecuteContext context;

    /**
     * Perform the mock before the UT is executed
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
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
        mockPlugEffectWhiteBlackUtils.close();
    }

    public MonoHttpConnectInterceptorTest() throws ClassNotFoundException, NoSuchMethodException {
        interceptor = new MonoHttpConnectInterceptor();
        monoHttpConnect = (Mono<?>) Mockito
                .mock(Class.forName("reactor.netty.http.client.HttpClientConnect$MonoHttpConnect"));
        configuration = Mockito.mock(Class.forName("reactor.netty.http.client.HttpClientConfiguration"));
        ReflectUtils.setFieldValue(configuration, "uri", HOST_URL);
        ReflectUtils.setFieldValue(monoHttpConnect, "configuration", configuration);
        Object[] arguments = new Object[1];
        arguments[0] = new CoreSubscriber() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(Object o) {
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        };
        context = ExecuteContext
                .forMemberMethod(monoHttpConnect, monoHttpConnect.getClass().getDeclaredMethod("subscribe",
                        CoreSubscriber.class), arguments, null, null);
    }

    @Test
    public void test() {
        // Test for normal conditions
        Mockito.doNothing().when(monoHttpConnect).subscribe(Mockito.isA(CoreSubscriber.class));
        interceptor.doBefore(context);
        Assert.assertEquals(configuration, context.getLocalFieldValue("originConfig"));
        Assert.assertEquals(HOST_URL, context.getLocalFieldValue("originUri"));
        Assert.assertEquals(INSTANCE_URL, ReflectUtils.getFieldValue(configuration, "uri").orElse(null));

        interceptor.after(context);
        Assert.assertEquals(HOST_URL, ReflectUtils.getFieldValue(configuration, "uri").orElse(null));
    }

    @Test
    public void testException() {
        // Test for anomalies
        Mockito.doThrow(new RuntimeException()).when(monoHttpConnect).subscribe(Mockito.isA(CoreSubscriber.class));
        interceptor.doBefore(context);
        Assert.assertEquals(RuntimeException.class, context.getThrowableOut().getClass());
        Assert.assertEquals(configuration, context.getLocalFieldValue("originConfig"));
        Assert.assertEquals(HOST_URL, context.getLocalFieldValue("originUri"));
        Assert.assertEquals(INSTANCE_URL, ReflectUtils.getFieldValue(configuration, "uri").orElse(null));

        interceptor.onThrow(context);
        Assert.assertEquals(HOST_URL, ReflectUtils.getFieldValue(configuration, "uri").orElse(null));
    }
}
