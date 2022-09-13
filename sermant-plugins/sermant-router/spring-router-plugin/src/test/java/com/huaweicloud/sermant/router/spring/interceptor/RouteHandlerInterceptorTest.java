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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.spring.cache.RequestData;
import com.huaweicloud.sermant.router.spring.cache.RequestHeader;
import com.huaweicloud.sermant.router.spring.service.RouteHandlerService;
import com.huaweicloud.sermant.router.spring.utils.ThreadLocalUtils;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测试RouteHandlerInterceptor
 *
 * @author provenceee
 * @since 2022-09-07
 */
public class RouteHandlerInterceptorTest {
    private final RouteHandlerInterceptor interceptor;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(RouteHandlerService.class))
            .thenReturn((RouteHandlerService) () -> {
                Set<String> keys = new HashSet<>();
                keys.add("bar");
                keys.add("foo");
                return keys;
            });

    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public RouteHandlerInterceptorTest() {
        interceptor = new RouteHandlerInterceptor();
    }

    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestHeader();
        ThreadLocalUtils.removeRequestData();
    }

    /**
     * 测试preHandle方法
     */
    @Test
    public void testPreHandle() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("bar", "bar1");
        request.addHeader("foo", "foo1");
        request.addHeader("foo2", "foo2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object obj = new Object();

        // 测试preHandle方法
        interceptor.preHandle(request, response, obj);
        RequestHeader requestHeader = ThreadLocalUtils.getRequestHeader();
        Map<String, List<String>> header = requestHeader.getHeader();
        Assert.assertNotNull(header);
        Assert.assertEquals(2, header.size());
        Assert.assertEquals("bar1", header.get("bar").get(0));
        Assert.assertEquals("foo1", header.get("foo").get(0));
    }

    /**
     * 测试afterCompletion,验证是否释放线程变量
     */
    @Test
    public void testAfterCompletion() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));

        // 测试afterCompletion,验证是否释放线程变量
        interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);
        Assert.assertNull(ThreadLocalUtils.getRequestHeader());
    }
}