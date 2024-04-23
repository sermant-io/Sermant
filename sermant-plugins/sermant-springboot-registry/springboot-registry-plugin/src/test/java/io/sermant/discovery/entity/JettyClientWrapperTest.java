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

package io.sermant.discovery.entity;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.config.LbConfig;
import io.sermant.discovery.utils.HttpConstants;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.Origin;
import org.eclipse.jetty.client.Origin.Address;
import org.eclipse.jetty.client.SendFailure;
import org.eclipse.jetty.client.api.Connection;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Result;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * Test JettyClientWrapper
 *
 * @author provenceee
 * @since 2023-05-17
 */
public class JettyClientWrapperTest {
    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private final JettyClientWrapper wrapper;

    private final HttpConversation conversation;

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

    public JettyClientWrapperTest() {
        HttpClient client = Mockito.mock(HttpClient.class);
        conversation = new HttpConversation();
        wrapper = new JettyClientWrapper(client, conversation, URI.create("http://www.domain.com/foo/hello"));

        // Since the host in the new JettyClientWrapper is related to the HttpClient
        // and the HttpClient is a mock object,
        // you need to manually set the host
        ReflectUtils.setFieldValue(wrapper, HttpConstants.HTTP_URI_HOST, "www.domain.com");
        ReflectUtils.setFieldValue(wrapper, "originHost", "www.domain.com");
    }

    @Test
    public void test() throws URISyntaxException {
        CompleteListener listener = new TestCompleteListener();
        // Test the send method
        wrapper.send(listener);
        Assert.assertEquals(listener, ReflectUtils.getFieldValue(wrapper, "originCompleteListener").orElse(null));

        // Data updates after simulating the send method
        conversation.getExchanges().add(initHttpExchangeInstance());
        conversation.updateResponseListeners(new TestCompleteListener());
        conversation.updateResponseListeners(listener);
        ReflectUtils.setFieldValue(wrapper, HttpConstants.HTTP_URI_HOST, "127.0.0.1");
        ReflectUtils.setFieldValue(wrapper, HttpConstants.HTTP_URI_PORT, 8080);
        ReflectUtils.setFieldValue(wrapper, HttpConstants.HTTP_URI_PATH, "hello");

        // Test the ABOR method
        wrapper.abort(new ConnectException());
        Assert.assertTrue(conversation.getExchanges().isEmpty());
        Assert.assertTrue(conversation.getResponseListeners().isEmpty());
        Assert.assertEquals("www.domain.com", wrapper.getHost());
        Assert.assertEquals("/foo/hello", wrapper.getPath());
        Assert.assertEquals(80, wrapper.getPort());
        Assert.assertEquals(listener, ReflectUtils.getFieldValue(wrapper, "originCompleteListener").orElse(null));
    }

    /**
     * Obtain an HttpExchange instance object to be supported by Jetty 9.4.53.v20231009
     *
     * @return Http Exchange instance object
     * @throws URISyntaxException
     */
    private HttpExchange initHttpExchangeInstance() throws URISyntaxException {
        Origin origin = new Origin("xxx", new Address("127.0.0.1", 11111));
        HttpDestination httpDestination = new HttpDestination(new HttpClient(), origin) {
            @Override
            protected SendFailure send(Connection connection, HttpExchange exchange) {
                return null;
            }
        };
        HttpRequest httpRequest = (HttpRequest) ReflectUtils.buildWithConstructor(HttpRequest.class,
                new Class[]{HttpClient.class, HttpConversation.class, URI.class},
                new Object[]{new HttpClient(), conversation, new URI("127.0.0.1")}).get();
        return new HttpExchange(httpDestination, httpRequest, Collections.emptyList());
    }

    public static class TestCompleteListener implements CompleteListener {
        @Override
        public void onComplete(Result result) {
        }
    }
}