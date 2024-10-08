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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.service.ServiceManager;
import io.sermant.router.common.request.RequestTag;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.BaseTransmitConfigTest;
import io.sermant.router.spring.TestSpringConfigService;
import io.sermant.router.spring.service.SpringConfigService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test AbstractHandlerMappingInterceptor
 *
 * @author provenceee
 * @since 2022-10-10
 */
public class AbstractHandlerMappingInterceptorTest extends BaseTransmitConfigTest {
    private final AbstractHandlerMappingInterceptor interceptor;

    private final ExecuteContext context;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        TestSpringConfigService configService = new TestSpringConfigService();
        configService.setReturnEmptyWhenGetMatchTags(true);
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class))
                .thenReturn(configService);
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public AbstractHandlerMappingInterceptorTest() {
        interceptor = new AbstractHandlerMappingInterceptor();
        Object[] arguments = new Object[1];
        MockServerHttpRequest request = MockServerHttpRequest.get("")
                .header("bar", "bar1").header("foo", "foo1").header("foo2", "foo2").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        arguments[0] = exchange;
        context = ExecuteContext.forMemberMethod(new RequestMappingHandlerMapping(), null, arguments, null, null);
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
        // Test the before method
        interceptor.before(context);
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        Map<String, List<String>> header = requestTag.getTag();
        Assert.assertNotNull(header);
        Assert.assertEquals(2, header.size());
        Assert.assertEquals("bar1", header.get("bar").get(0));
        Assert.assertEquals("foo1", header.get("foo").get(0));
    }

    /**
     * Test the after method to verify whether the thread variable is released
     */
    @Test
    public void testAfter() {
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("foo")));
        Assert.assertNotNull(ThreadLocalUtils.getRequestTag());

        // Test the after method without releasing thread variables
        interceptor.after(context);
        Assert.assertNotNull(ThreadLocalUtils.getRequestTag());
    }

    /**
     * Test the onThrow method to verify whether the thread variable is released
     */
    @Test
    public void testOnThrow() {
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("foo")));
        Assert.assertNotNull(ThreadLocalUtils.getRequestTag());

        // Test the on Throw method to verify whether the thread variable is released
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }
}
