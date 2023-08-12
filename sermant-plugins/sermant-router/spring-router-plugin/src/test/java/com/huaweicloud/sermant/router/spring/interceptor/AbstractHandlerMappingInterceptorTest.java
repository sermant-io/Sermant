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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.BaseTransmitConfigTest;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测试AbstractHandlerMappingInterceptor
 *
 * @author provenceee
 * @since 2022-10-10
 */
public class AbstractHandlerMappingInterceptorTest extends BaseTransmitConfigTest {
    private final AbstractHandlerMappingInterceptor interceptor;

    private final ExecuteContext context;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class))
            .thenReturn(new SpringConfigService() {
                @Override
                public void init(String cacheName, String serviceName) {
                }

                @Override
                public Set<String> getMatchKeys() {
                    Set<String> keys = new HashSet<>();
                    keys.add("bar");
                    keys.add("foo");
                    return keys;
                }
            });
    }

    /**
     * UT执行后释放mock对象
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
     * 重置测试数据
     */
    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestHeader();
        ThreadLocalUtils.removeRequestData();
    }

    /**
     * 测试before方法
     */
    @Test
    public void testBefore() {
        // 测试before方法
        interceptor.before(context);
        RequestHeader requestHeader = ThreadLocalUtils.getRequestHeader();
        Map<String, List<String>> header = requestHeader.getHeader();
        Assert.assertNotNull(header);
        Assert.assertEquals(2, header.size());
        Assert.assertEquals("bar1", header.get("bar").get(0));
        Assert.assertEquals("foo1", header.get("foo").get(0));
    }

    /**
     * 测试after方法,验证是否释放线程变量
     */
    @Test
    public void testAfter() {
        ThreadLocalUtils.setRequestHeader(new RequestHeader(Collections.emptyMap()));

        // 测试after方法,不释放线程变量
        interceptor.after(context);
        Assert.assertNotNull(ThreadLocalUtils.getRequestHeader());
    }

    /**
     * 测试onThrow方法,验证是否释放线程变量
     */
    @Test
    public void testOnThrow() {
        ThreadLocalUtils.setRequestHeader(new RequestHeader(Collections.emptyMap()));

        // 测试onThrow方法,验证是否释放线程变量
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestHeader());
    }
}