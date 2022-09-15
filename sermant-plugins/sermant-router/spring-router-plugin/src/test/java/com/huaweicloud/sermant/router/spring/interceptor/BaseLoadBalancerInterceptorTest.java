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

import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.spring.cache.RequestData;
import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;
import com.huaweicloud.sermant.router.spring.utils.ThreadLocalUtils;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.zuul.context.RequestContext;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试BaseLoadBalancerInterceptor
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class BaseLoadBalancerInterceptorTest {
    private final BaseLoadBalancerInterceptor interceptor;

    private final ExecuteContext context;

    private final BaseLoadBalancer loadBalancer;

    private static TestSpringConfigService configService;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstant.LOG_SETTING_FILE_KEY,
            ServiceRegistryInterceptorTest.class.getResource("/logback-test.xml").getPath());
        LoggerFactory.init(map);
        configService = new TestSpringConfigService();
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class)).thenReturn(configService);
        mockServiceManager.when(() -> ServiceManager.getService(LoadBalancerService.class))
            .thenReturn(new TestLoadBalancerService());
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public BaseLoadBalancerInterceptorTest() throws NoSuchMethodException {
        interceptor = new BaseLoadBalancerInterceptor();
        loadBalancer = new BaseLoadBalancer();
        context = ExecuteContext.forMemberMethod(loadBalancer, String.class.getMethod("trim"), null, null,
            null);
    }

    @Before
    public void reset() {
        ThreadLocalUtils.removeRequestHeader();
        ThreadLocalUtils.removeRequestData();
        configService.setInvalid(false);
        List<Server> servers = new ArrayList<>();
        servers.add(new Server("bar", 8080));
        servers.add(new Server("foo", 8081));
        loadBalancer.setServersList(servers);
    }

    /**
     * 测试路由规则无效时
     */
    @Test
    public void testBeforeWhenInvalid() {
        configService.setInvalid(true);
        interceptor.before(context);
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) context.getObject();
        List<Server> servers = loadBalancer.getAllServers();
        Assert.assertNotNull(servers);
        Assert.assertEquals(2, servers.size());
    }

    /**
     * 测试实例列表为空时
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
     * 测试从ThreadLocal获取请求数据
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
     * 测试从RequestContext获取请求数据
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