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

package com.huaweicloud.sermant.router.dubbo.handler;

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.dubbo.TestDubboConfigService;
import com.huaweicloud.sermant.router.dubbo.service.DubboConfigService;

import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 测试RouteContextFilterHandler
 *
 * @author provenceee
 * @since 2023-02-25
 */
public class RouteContextFilterHandlerTest {
    private static final TestDubboConfigService DUBBO_CONFIG_SERVICE = new TestDubboConfigService();

    private static MockedStatic<ServiceManager> mockServiceManager;

    private final RouteContextFilterHandler routeContextFilterHandler;

    private final RpcInvocation invocation;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(DubboConfigService.class))
                .thenReturn(DUBBO_CONFIG_SERVICE);
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public RouteContextFilterHandlerTest() {
        routeContextFilterHandler = new RouteContextFilterHandler();
        invocation = new RpcInvocation();
        invocation.setAttachmentIfAbsent("bar", "bar1");
        invocation.setAttachmentIfAbsent("foo", "foo1");
        invocation.setAttachmentIfAbsent("foo2", "foo2");
    }

    @Test
    public void testGetRequestTag() {
        // 正常情况
        DUBBO_CONFIG_SERVICE.setReturnEmptyWhenGetMatchKeys(false);
        Map<String, List<String>> requestTag = routeContextFilterHandler.getRequestTag(null, invocation);
        Assert.assertNotNull(requestTag);
        Assert.assertEquals(2, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));

        // 测试getMatchKeys返回空
        DUBBO_CONFIG_SERVICE.setReturnEmptyWhenGetMatchKeys(true);
        requestTag = routeContextFilterHandler.getRequestTag(null, invocation);
        Assert.assertEquals(Collections.emptyMap(), requestTag);
    }
}