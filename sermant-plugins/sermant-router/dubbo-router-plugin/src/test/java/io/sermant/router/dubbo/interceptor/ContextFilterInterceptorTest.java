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

package io.sermant.router.dubbo.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.router.common.config.TransmitConfig;
import io.sermant.router.common.request.RequestTag;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.dubbo.TestDubboConfigService;
import io.sermant.router.dubbo.service.DubboConfigService;

import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test ContextFilterInterceptor
 *
 * @author provenceee
 * @since 2022-10-10
 */
public class ContextFilterInterceptorTest {
    private final ContextFilterInterceptor interceptor;

    private final ExecuteContext context;

    private static MockedStatic<ServiceManager> mockServiceManager;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    /**
     * Mock before UT execution
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        TestDubboConfigService testDubboConfigService = new TestDubboConfigService();
        testDubboConfigService.setReturnEmptyWhenGetInjectTags(true);
        mockServiceManager.when(() -> ServiceManager.getService(DubboConfigService.class))
                .thenReturn(testDubboConfigService);

        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(TransmitConfig.class))
                .thenReturn(new TransmitConfig());
    }

    /**
     * Release all mock objects after UT execution
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
        mockPluginConfigManager.close();
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
     * reset the test data
     */
    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestTag();
    }

    /**
     * Test before method
     */
    @Test
    public void testBefore() {
        // Test before method
        interceptor.before(context);
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        Map<String, List<String>> header = requestTag.getTag();
        Assert.assertNotNull(header);
        Assert.assertEquals(2, header.size());
        Assert.assertEquals("bar1", header.get("bar").get(0));
        Assert.assertEquals("foo1", header.get("foo").get(0));
    }

    /**
     * Test the after method to verify if thread variables are released
     */
    @Test
    public void testAfter() {
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("foo")));
        Assert.assertNotNull(ThreadLocalUtils.getRequestTag());

        // Test the after method to verify if thread variables are released
        interceptor.after(context);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }

    /**
     * Test the onThrow method to verify if thread variables are released
     */
    @Test
    public void testOnThrow() {
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("foo")));
        Assert.assertNotNull(ThreadLocalUtils.getRequestTag());

        // Test the onThrow method to verify if thread variables are released
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }
}