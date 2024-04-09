/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.BaseTransmitConfigTest;
import com.huaweicloud.sermant.router.spring.TestSpringConfigService;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test RouteHandlerInterceptor
 *
 * @author provenceee
 * @since 2022-09-07
 */
public class RouteHandlerInterceptorTest extends BaseTransmitConfigTest {
    private final RouteHandlerInterceptor interceptor;

    private static TestSpringConfigService configService;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        configService = new TestSpringConfigService();
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

    public RouteHandlerInterceptorTest() {
        interceptor = new RouteHandlerInterceptor();
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
     * Test the preHandle method
     */
    @Test
    public void testPreHandle() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("bar", "bar1");
        request.addHeader("foo", "foo1");
        request.addHeader("foo2", "foo2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object obj = new Object();

        // The test keys are all empty
        configService.setReturnEmptyWhenGetMatchTags(true);
        configService.setReturnEmptyWhenGetMatchKeys(true);
        interceptor.preHandle(request, response, obj);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());

        // Test the preHandle method, getMatchKeys is not empty
        configService.setReturnEmptyWhenGetMatchTags(true);
        configService.setReturnEmptyWhenGetMatchKeys(false);
        interceptor.preHandle(request, response, obj);
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        Map<String, List<String>> header = requestTag.getTag();
        Assert.assertNotNull(header);
        Assert.assertEquals(2, header.size());
        Assert.assertEquals("bar1", header.get("bar").get(0));
        Assert.assertEquals("foo1", header.get("foo").get(0));
    }

    /**
     * After testing is completed, verify if the thread variables are released
     */
    @Test
    public void testAfterCompletion() {
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("foo")));
        Assert.assertNotNull(ThreadLocalUtils.getRequestTag());

        // Test afterCompletion to verify if thread variables are released
        interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }
}