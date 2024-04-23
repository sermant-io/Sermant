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

package io.sermant.discovery.interceptors;

import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.discovery.config.PlugEffectWhiteBlackConstants;
import io.sermant.discovery.entity.PlugEffectStrategyCache;
import io.sermant.discovery.service.InvokerService;

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
 * feign calls the test
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
     * Constructor
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
        Optional<Object> dynamicConfig = ReflectUtils.getFieldValue(PlugEffectStrategyCache.INSTANCE, "caches");
        Assert.assertTrue(dynamicConfig.isPresent() && dynamicConfig.get() instanceof Map);
        ((Map) dynamicConfig.get()).put(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY, strategy);
        ((Map) dynamicConfig.get()).put(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_VALUE, serviceName);
    }

    @Test
    public void testFeignInvokeInterceptor() throws Exception {
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
        Request request = createRequest(HttpMethod.GET, url);
        arguments[0] = request;

        // No domain name is configured in the environment
        interceptor.doBefore(context);
        Request temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        // Contains domain names, sets a single domain name, and does not set a blacklist or whitelist
        discoveryPluginConfig.setRealmName(realmName);
        interceptor.doBefore(context);
        temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        // Contains domain names, sets multiple domain names, and does not set blacklist or whitelist
        discoveryPluginConfig.setRealmName(realmNames);
        interceptor.doBefore(context);
        temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        // Contains domain names, and sets all do not pass the policy
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_NONE, "");
        interceptor.doBefore(context);
        temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        // Contains domain names, and sets the blacklist to include the corresponding service name
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_BLACK, "zookeeper-provider-demo");
        interceptor.doBefore(context);
        temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        // Contains domain names, and the blacklist does not contain the corresponding service name
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_BLACK, "service1");
        Mockito.when(invokerService.invoke(null, null, "zookeeper-provider-demo"))
                .thenReturn(Optional.ofNullable(new Object()));
        interceptor.doBefore(context);
        temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        // Contains the domain name, and sets the whitelist to include the corresponding service name
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_WHITE, "zookeeper-provider-demo");
        Mockito.when(invokerService.invoke(null, null, "zookeeper-provider-demo"))
                .thenReturn(Optional.ofNullable(new Object()));
        interceptor.doBefore(context);
        temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        // If the domain name is included, the whitelist does not contain the corresponding service name
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_WHITE, "service1");
        interceptor.doBefore(context);
        temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());

        // Contains domain names, and sets all through policies
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_ALL, "service1");
        Mockito.when(invokerService.invoke(null, null, "zookeeper-provider-demo"))
                .thenReturn(Optional.ofNullable(new Object()));
        interceptor.doBefore(context);
        temp = (Request) context.getArguments()[0];
        Assert.assertEquals(url, temp.url());
    }

    @Test
    public void buildErrorResponseTest() throws IOException {
        Optional<Method> method = ReflectUtils.findMethod(FeignInvokeInterceptor.class, "buildErrorResponse",
                new Class[]{Exception.class, Request.class});
        Exception ex = new Exception();
        Request request = createRequest(HttpMethod.GET, url);
        if (method.isPresent()) {
            Optional<Object> exception = ReflectUtils
                    .invokeMethod(interceptor, method.get(), new Object[]{ex, request});
            Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, ((Response) exception.get()).status());
        }
    }
}
