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

import com.huaweicloud.sermant.router.dubbo.TestDubboConfigService;

import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.Assert;
import org.junit.Test;

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
    private final TestDubboConfigService configService;

    private final RouteContextFilterHandler routeContextFilterHandler;

    private final RpcInvocation invocation;

    public RouteContextFilterHandlerTest() {
        routeContextFilterHandler = new RouteContextFilterHandler();
        configService = new TestDubboConfigService();
        invocation = new RpcInvocation();
        invocation.setAttachmentIfAbsent("bar", "bar1");
        invocation.setAttachmentIfAbsent("foo", "foo1");
        invocation.setAttachmentIfAbsent("foo2", "foo2");
    }

    @Test
    public void testGetRequestTag() {
        // 正常情况
        configService.setReturnEmptyWhenGetMatchKeys(false);
        Map<String, List<String>> requestTag = routeContextFilterHandler
                .getRequestTag(null, invocation, invocation.getObjectAttachments(),
                        configService.getMatchKeys(), configService.getInjectTags());
        Assert.assertNotNull(requestTag);
        Assert.assertEquals(2, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));

        // 测试getMatchKeys返回空
        configService.setReturnEmptyWhenGetMatchKeys(true);
        requestTag = routeContextFilterHandler.getRequestTag(null, invocation, invocation.getObjectAttachments(),
                configService.getMatchKeys(), configService.getInjectTags());
        Assert.assertEquals(Collections.emptyMap(), requestTag);
    }
}