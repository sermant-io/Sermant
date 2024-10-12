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
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.config.TransmitConfig;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.service.LoadBalancerService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test NopInstanceFilterInterceptor
 *
 * @author provenceee
 * @since 2022-10-09
 */
public class NopInstanceFilterInterceptorTest {
    private final NopInstanceFilterInterceptor interceptor;

    private final ExecuteContext context;

    private final Object[] arguments;

    private static MockedStatic<ServiceManager> mockServiceManager;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(LoadBalancerService.class))
                .thenReturn(new TestLoadBalancerService());

        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RouterConfig.class))
                .thenReturn(new RouterConfig());
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(TransmitConfig.class))
                .thenReturn(new TransmitConfig());
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
        mockPluginConfigManager.close();
    }

    public NopInstanceFilterInterceptorTest() throws NoSuchMethodException {
        interceptor = new NopInstanceFilterInterceptor();
        arguments = new Object[2];
        context = ExecuteContext.forMemberMethod(new Object(), String.class.getMethod("trim"), arguments, null, null);
    }

    /**
     * Test the getTargetExamples method
     */
    @Test
    public void testGetTargetInstances() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        List<ServiceInstance> list = new ArrayList<>();
        DefaultServiceInstance instance1 = new DefaultServiceInstance("foo1", "foo", "foo", 8080, false);
        list.add(instance1);
        DefaultServiceInstance instance2 = new DefaultServiceInstance("bar2", "foo", "bar", 8081, false);
        list.add(instance2);
        arguments[0] = "foo";
        arguments[1] = list;
        List<?> result = (List<?>) interceptor.before(context).getResult();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(instance2, result.get(0));
    }

    @Test
    public void testAfter() {
        ThreadLocalUtils.setRequestData(new RequestData(null, "", ""));
        interceptor.after(context);
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }

    @Test
    public void testOnThrow() {
        ThreadLocalUtils.setRequestData(new RequestData(null, "", ""));
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }
}
