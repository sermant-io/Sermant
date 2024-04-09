/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.TransmitConfig;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test ReactiveLoadBalancerClientFilterInterceptor
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class ReactiveLoadBalancerClientFilterInterceptorTest {
    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private final ReactiveLoadBalancerClientFilterInterceptor interceptor;

    private final ExecuteContext context;

    public ReactiveLoadBalancerClientFilterInterceptorTest() {
        interceptor = new ReactiveLoadBalancerClientFilterInterceptor();
        Object[] arguments = new Object[1];
        MockServerHttpRequest request = MockServerHttpRequest.get("")
                .header("bar", "bar1").header("foo", "foo1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        arguments[0] = exchange;
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void initTransmitConfig() {
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        TransmitConfig transmitConfig = new TransmitConfig();
        transmitConfig.setEnabledThreadPool(true);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(TransmitConfig.class))
                .thenReturn(transmitConfig);
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void closeMock() {
        mockPluginConfigManager.close();
    }

    /**
     * Reset the test data
     */
    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
    }

    /**
     * Test the before method
     */
    @Test
    public void testBefore() {
        // When RequestTag is null
        interceptor.before(context);
        RequestData requestData = ThreadLocalUtils.getRequestData();
        Assert.assertEquals(HttpMethod.GET.name(), requestData.getHttpMethod());
        Assert.assertEquals("", requestData.getPath());
        Assert.assertNotNull(requestData);
        Map<String, List<String>> headerData = requestData.getTag();
        Assert.assertEquals(2, headerData.size());
        Assert.assertEquals("bar1", headerData.get("bar").get(0));
        Assert.assertEquals("foo1", headerData.get("foo").get(0));

        // When RequestTag is not null
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar-foo", Collections.singletonList("foo2")));
        interceptor.before(context);
        requestData = ThreadLocalUtils.getRequestData();
        Assert.assertEquals(HttpMethod.GET.name(), requestData.getHttpMethod());
        Assert.assertEquals("", requestData.getPath());
        Assert.assertNotNull(requestData);
        headerData = requestData.getTag();
        Assert.assertEquals(3, headerData.size());
        Assert.assertEquals("bar1", headerData.get("bar").get(0));
        Assert.assertEquals("foo1", headerData.get("foo").get(0));
        Assert.assertEquals("foo2", headerData.get("bar-foo").get(0));
    }

    /**
     * Test the after method
     */
    @Test
    public void testAfter() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.after(context);
        Assert.assertNotNull(ThreadLocalUtils.getRequestData());
    }

    /**
     * Test the onThrow method
     */
    @Test
    public void testOnThrow() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }
}