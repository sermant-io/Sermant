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
import io.sermant.core.service.ServiceManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.TestInvokerService;
import io.sermant.discovery.config.DiscoveryPluginConfig;
import io.sermant.discovery.config.LbConfig;
import io.sermant.discovery.entity.JettyClientWrapper;
import io.sermant.discovery.entity.SimpleRequestRecorder;
import io.sermant.discovery.service.InvokerService;
import io.sermant.discovery.utils.HttpConstants;
import io.sermant.discovery.utils.RequestInterceptorUtils;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Result;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Optional;

/**
 * Test JettyRequestInterceptor
 *
 * @author provenceee
 * @since 2023-05-17
 */
public class JettyRequestInterceptorTest {
    private static final URI HELLO_URI = URI.create("http://www.domain.com/bar/hello?name=foo");

    private static MockedStatic<ServiceManager> mockServiceManager;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private final JettyRequestInterceptor interceptor;

    private final Object[] arguments;

    private final Method method;

    /**
     * Perform the mock before the UT is executed
     */
    @BeforeClass
    public static void before() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(InvokerService.class))
                .thenReturn(new TestInvokerService());

        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(new LbConfig());

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
        mockPluginConfigManager.close();
    }

    public JettyRequestInterceptorTest() throws NoSuchMethodException {
        interceptor = new JettyRequestInterceptor();
        arguments = new Object[1];
        arguments[0] = new CompleteListener() {
            @Override
            public void onComplete(Result result) {
            }
        };
        method = JettyClientWrapper.class.getDeclaredMethod("send", CompleteListener.class);
    }

    @Test
    public void test() {
        // Test for normal conditions
        JettyClientWrapper wrapper = Mockito.spy(new JettyClientWrapper(Mockito.mock(HttpClient.class),
                new HttpConversation(), HELLO_URI));
        ReflectUtils.setFieldValue(wrapper, HttpConstants.HTTP_URI_HOST, "www.domain.com");
        ExecuteContext context = ExecuteContext.forMemberMethod(wrapper, method, arguments, null, null);
        Mockito.doNothing().when(wrapper).send(Mockito.isA(CompleteListener.class));
        interceptor.doBefore(context);
        Assert.assertEquals("127.0.0.1", wrapper.getHost());
        Assert.assertEquals(8010, wrapper.getPort());
        Assert.assertEquals("/hello", wrapper.getPath());
    }

    @Test
    public void testException() {
        // Test for anomalies
        JettyClientWrapper wrapper = Mockito.spy(new JettyClientWrapper(Mockito.mock(HttpClient.class),
                new HttpConversation(), HELLO_URI));
        ReflectUtils.setFieldValue(wrapper, HttpConstants.HTTP_URI_HOST, "www.domain.com");
        ExecuteContext context = ExecuteContext.forMemberMethod(wrapper, method, arguments, null, null);
        Mockito.doThrow(new RuntimeException()).when(wrapper).send(Mockito.isA(CompleteListener.class));
        interceptor.doBefore(context);
        Assert.assertEquals(RuntimeException.class, context.getThrowableOut().getClass());
    }
}
