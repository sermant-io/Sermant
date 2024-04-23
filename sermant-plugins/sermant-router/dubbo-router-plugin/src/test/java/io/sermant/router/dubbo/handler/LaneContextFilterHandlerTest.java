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

package io.sermant.router.dubbo.handler;

import io.sermant.core.service.ServiceManager;
import io.sermant.router.dubbo.TestDubboConfigService;
import io.sermant.router.dubbo.service.LaneContextFilterService;

import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test LaneContextFilterHandler
 *
 * @author provenceee
 * @since 2023-02-25
 */
public class LaneContextFilterHandlerTest {
    private static final TestLaneContextFilterService LANE_CONTEXT_FILTER_SERVICE = new TestLaneContextFilterService();

    private static MockedStatic<ServiceManager> mockServiceManager;

    private final LaneContextFilterHandler laneContextFilterHandler;

    private final TestDubboConfigService configService;

    private final RpcInvocation invocation;

    private final ApacheInvoker<?> invoker;

    private static final org.apache.dubbo.common.URL APACHE_URL = org.apache.dubbo.common.URL
            .valueOf("dubbo://localhost:8081/io.sermant.foo.FooTest?foo=bar&version=0.0.1");

    /**
     * Mock before UT execution
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(LaneContextFilterService.class))
                .thenReturn(LANE_CONTEXT_FILTER_SERVICE);
    }

    /**
     * Release all mock objects after UT execution
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public LaneContextFilterHandlerTest() {
        laneContextFilterHandler = new LaneContextFilterHandler();
        configService = new TestDubboConfigService();
        invocation = new RpcInvocation();
        invocation.setAttachmentIfAbsent("bar", "bar1");
        invocation.setAttachmentIfAbsent("foo", "foo1");
        invocation.setAttachmentIfAbsent("foo2", "foo2");
        invocation.setMethodName("getFoo");
        invocation.setArguments(new Object[0]);
        invoker = new ApacheInvoker<>();
    }

    @Test
    public void testGetRequestTag() {
        // Test getMatchTags returns null
        configService.setReturnEmptyWhenGetInjectTags(true);
        Map<String, List<String>> requestTag = laneContextFilterHandler.getRequestTag(invoker, invocation,
                invocation.getObjectAttachments(), configService.getMatchKeys(), configService.getInjectTags());
        Assert.assertEquals(requestTag, Collections.emptyMap());

        // the test getLane returns null
        configService.setReturnEmptyWhenGetInjectTags(false);
        LANE_CONTEXT_FILTER_SERVICE.setReturnEmpty(true);
        requestTag = laneContextFilterHandler.getRequestTag(invoker, invocation, invocation.getObjectAttachments(),
                configService.getMatchKeys(), configService.getInjectTags());
        Assert.assertEquals(2, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));

        // test that the getLane is not empty
        configService.setReturnEmptyWhenGetInjectTags(false);
        LANE_CONTEXT_FILTER_SERVICE.setReturnEmpty(false);
        requestTag = laneContextFilterHandler.getRequestTag(invoker, invocation, invocation.getObjectAttachments(),
                configService.getMatchKeys(), configService.getInjectTags());
        Assert.assertEquals(3, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));
        Assert.assertEquals("flag1", requestTag.get("sermant-flag").get(0));
    }

    public static class TestLaneContextFilterService implements LaneContextFilterService {
        private boolean returnEmpty;

        @Override
        public Map<String, List<String>> getLane(String interfaceName, String methodName,
                Map<String, Object> attachments, Object[] args) {
            if (returnEmpty) {
                return Collections.emptyMap();
            }
            Map<String, List<String>> map = new HashMap<>();
            map.put("sermant-flag", Collections.singletonList("flag1"));
            map.put("bar", Collections.singletonList("bar2"));
            return map;
        }

        public void setReturnEmpty(boolean returnEmpty) {
            this.returnEmpty = returnEmpty;
        }
    }

    public static class ApacheInvoker<T> implements org.apache.dubbo.rpc.Invoker<T> {
        @Override
        public Class<T> getInterface() {
            return null;
        }

        @Override
        public org.apache.dubbo.rpc.Result invoke(org.apache.dubbo.rpc.Invocation invocation)
                throws org.apache.dubbo.rpc.RpcException {
            return null;
        }

        @Override
        public org.apache.dubbo.common.URL getUrl() {
            return APACHE_URL;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void destroy() {
        }
    }
}