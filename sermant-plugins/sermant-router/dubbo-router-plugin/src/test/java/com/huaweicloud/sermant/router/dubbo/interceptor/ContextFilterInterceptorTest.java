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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.dubbo.service.DubboConfigService;

import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测试ContextFilterInterceptor
 *
 * @author provenceee
 * @since 2022-10-10
 */
public class ContextFilterInterceptorTest {
    private final ContextFilterInterceptor interceptor;

    private final ExecuteContext context;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(DubboConfigService.class))
            .thenReturn(new DubboConfigService() {
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

    public ContextFilterInterceptorTest() throws NoSuchMethodException {
        interceptor = new ContextFilterInterceptor();
        Object[] arguments = new Object[2];
        RpcInvocation invocation = new RpcInvocation();
        invocation.setAttachmentIfAbsent("bar", "bar1");
        invocation.setAttachmentIfAbsent("foo", "foo1");
        invocation.setAttachmentIfAbsent("foo2", "foo2");
        arguments[1] = invocation;
        context = ExecuteContext.forMemberMethod(new Object(), String.class.getMethod("trim"), arguments, null, null);
    }

    /**
     * 重置测试数据
     */
    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestHeader();
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
        Assert.assertNotNull(ThreadLocalUtils.getRequestHeader());

        // 测试after方法,验证是否释放线程变量
        interceptor.after(context);
        Assert.assertNull(ThreadLocalUtils.getRequestHeader());
    }

    /**
     * 测试onThrow方法,验证是否释放线程变量
     */
    @Test
    public void testOnThrow() {
        ThreadLocalUtils.setRequestHeader(new RequestHeader(Collections.emptyMap()));
        Assert.assertNotNull(ThreadLocalUtils.getRequestHeader());

        // 测试onThrow方法,验证是否释放线程变量
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestHeader());
    }
}