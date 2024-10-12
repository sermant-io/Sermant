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

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.zuul.context.RequestContext;

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test BaseLoadBalancerInterceptor
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class BaseLoadBalancerInterceptorTest {
    private final BaseLoadBalancerInterceptor interceptor;

    private final ExecuteContext context;

    private final BaseLoadBalancer loadBalancer;

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

    public BaseLoadBalancerInterceptorTest() throws NoSuchMethodException {
        interceptor = new BaseLoadBalancerInterceptor();
        loadBalancer = new BaseLoadBalancer();
        context = ExecuteContext.forMemberMethod(loadBalancer, String.class.getMethod("trim"), null, null,
                null);
    }

    /**
     * Reset the test data
     */
    @Before
    public void reset() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
        //        configService.setInvalid(false);
        List<Server> servers = new ArrayList<>();
        servers.add(new Server("bar", 8080));
        servers.add(new Server("foo", 8081));
        loadBalancer.setServersList(servers);
    }

    /**
     * When the test routing rule is invalid
     */
    @Test
    public void testBeforeWhenInvalid() {
        //        configService.setInvalid(true);
        interceptor.before(context);
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) context.getObject();
        List<Server> servers = loadBalancer.getAllServers();
        Assert.assertNotNull(servers);
        Assert.assertEquals(2, servers.size());
    }

    /**
     * When the test instance list is empty
     */
    @Test
    public void testBeforeWithEmptyServers() {
        loadBalancer.setServersList(Collections.emptyList());
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.before(context);
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) context.getObject();
        List<Server> servers = loadBalancer.getAllServers();
        Assert.assertNotNull(servers);
        Assert.assertEquals(0, servers.size());
    }

    /**
     * The test fetches the request data from the ThreadLocal
     */
    @Test
    public void testBeforeWithThreadLocal() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.before(context);
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) context.getObject();
        List<Server> servers = loadBalancer.getAllServers();
        Assert.assertNotNull(servers);
        Assert.assertEquals(1, servers.size());
        Assert.assertEquals("foo", servers.get(0).getHost());
        Assert.assertEquals(8081, servers.get(0).getPort());
    }

    /**
     * The test gets the request data from the RequestContext
     */
    @Test
    public void testBeforeWithRequestContext() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("bar", "bar1");
        request.addHeader("foo", "foo1");
        request.addHeader("foo2", "foo2");
        requestContext.setRequest(request);
        interceptor.before(context);
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) context.getObject();
        List<Server> servers = loadBalancer.getAllServers();
        Assert.assertNotNull(servers);
        Assert.assertEquals(1, servers.size());
        Assert.assertEquals("foo", servers.get(0).getHost());
        Assert.assertEquals(8081, servers.get(0).getPort());
    }
}
