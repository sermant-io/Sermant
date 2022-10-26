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

package com.huawei.discovery.interceptors;

import com.huawei.discovery.config.PlugEffectWhiteBlackConstants;
import com.huawei.discovery.entity.PlugEffectStategyCache;
import com.huawei.discovery.service.InvokerService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * feign调用测试
 *
 * @author chengyouling
 * @since 2022-10-10
 */
public class FeignInvokeInterceptorTest extends BaseTest {

    private FeignInvokeInterceptor interceptor;

    private final Object[] arguments;

    @Mock
    private InvokerService invokerService;

    private final String realmName = "www.domain.com";

    private final String realmNames = "www.domain.com,www.domain2.com";

    private final static String url = "http://www.domain.com/zookeeper-provider-demo/sayHello?name=123";

    /**
     * 构造方法
     */
    public FeignInvokeInterceptorTest() {
        arguments = new Object[2];
    }

    @Override
    public void setUp() {
        super.setUp();
        interceptor = new FeignInvokeInterceptor();
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(InvokerService.class))
                .thenReturn(invokerService);
    }

    private Request createRequest(HttpMethod httpMethod, String url) {
        return Request.create(httpMethod, url, new HashMap<String, Collection<String>>(), null);
    }

    private void initStrategy(String strategy, String serviceName) {
        Optional<Object> dynamicConfig = ReflectUtils.getFieldValue(PlugEffectStategyCache.INSTANCE, "caches");
        Assert.assertTrue(dynamicConfig.isPresent() && dynamicConfig.get() instanceof Map);
        ((Map) dynamicConfig.get()).put(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY, strategy);
        ((Map) dynamicConfig.get()).put(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_VALUE, serviceName);

    }

    @Test
    public void testFeignInvokeInterceptor() throws Exception {
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
        Request request = createRequest(HttpMethod.GET, url);
        arguments[0] = request;

        //环境中未配置域名
        interceptor.doBefore(context);
        Request temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        //含域名，设置单个域名，未设置黑白名单
        discoveryPluginConfig.setRealmName(realmName);
        interceptor.doBefore(context);
        temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        //含域名，设置多个域名，未设置黑白名单
        discoveryPluginConfig.setRealmName(realmNames);
        interceptor.doBefore(context);
        temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        //含域名，设置全部不通过策略
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_NONE, "");
        interceptor.doBefore(context);
        temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        //含域名，设置黑名单包含对应服务名
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_BLACK, "zookeeper-provider-demo");
        interceptor.doBefore(context);
        temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        //含域名，设置黑名单不包含对应服务名
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_BLACK, "service1");
        Mockito.when(invokerService.invoke(null, null, "zookeeper-provider-demo"))
                .thenReturn(Optional.ofNullable(new Object()));
        interceptor.doBefore(context);
        temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        //含域名，设置白名单包含对应服务名
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_WHITE, "zookeeper-provider-demo");
        Mockito.when(invokerService.invoke(null, null, "zookeeper-provider-demo"))
                .thenReturn(Optional.ofNullable(new Object()));
        interceptor.doBefore(context);
        temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        //含域名，设置白名单不包含对应服务名
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_WHITE, "service1");
        interceptor.doBefore(context);
        temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        //含域名，设置全部通过策略
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_ALL, "service1");
        Mockito.when(invokerService.invoke(null, null, "zookeeper-provider-demo"))
                .thenReturn(Optional.ofNullable(new Object()));
        interceptor.doBefore(context);
        temp = (Request)context.getArguments()[0];
        Assert.assertEquals(url, temp.url());
    }

    @Test
    public void buildErrorResponseTest() throws IOException {
        Optional<Method> method = ReflectUtils.findMethod(FeignInvokeInterceptor.class, "buildErrorResponse",
                new Class[] {Exception.class, Request.class});
        Exception ex = new Exception();
        Request request = createRequest(HttpMethod.GET, url);
        if (method.isPresent()) {
            Optional<Object> exception = ReflectUtils
                    .invokeMethod(interceptor, method.get(), new Object[] {ex, request});
            Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, ((Response)exception.get()).status());
        }
    }
}
