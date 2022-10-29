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
import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.entity.PlugEffectStategyCache;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OkHttp调用测试
 *
 * @author chengyouling
 * @since 2022-10-10
 */
public class OkHttpClientInterceptorTest extends BaseTest {

    private OkHttpClientInterceptor interceptor;

    private final Object[] arguments;

    @Mock
    private InvokerService invokerService;

    private final String realmName = "www.domain.com";

    private final static String url = "http://www.domain.com/zookeeper-provider-demo/sayHello?name=123";

    private final static String convertUrl = "http://127.0.0.1:8010/sayHello?name=123";

    /**
     * 构造方法
     */
    public OkHttpClientInterceptorTest() {
        arguments = new Object[2];
    }

    @Override
    public void setUp() {
        super.setUp();
        interceptor = new OkHttpClientInterceptor();
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(InvokerService.class))
                .thenReturn(invokerService);
    }

    private Request createRequest(String url) {
        HttpUrl newUrl = HttpUrl.parse(url);
        return new Request.Builder()
                .url(newUrl)
                .build();
    }

    private void initStrategy(String strategy, String serviceName) {
        Optional<Object> dynamicConfig = ReflectUtils.getFieldValue(PlugEffectStategyCache.INSTANCE, "caches");
        Assert.assertTrue(dynamicConfig.isPresent() && dynamicConfig.get() instanceof Map);
        ((Map) dynamicConfig.get()).put(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY, strategy);
        ((Map) dynamicConfig.get()).put(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_VALUE, serviceName);

    }

    @Test
    public void testRestTemplateInterceptor() throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = createRequest(url);
        ExecuteContext context = ExecuteContext.forMemberMethod(client.newCall(request),
                String.class.getDeclaredMethod("trim"), arguments, null,
                null);
        discoveryPluginConfig.setRealmName(realmName);
        initStrategy(PlugEffectWhiteBlackConstants.STRATEGY_ALL, "zookeeper-provider-demo");
        interceptor.doBefore(context);
        Request requestNew = (Request)context.getRawMemberFieldValue("originalRequest");
        Assert.assertEquals(url, requestNew.uri().toString());
    }

    @Test
    public void covertRequestTest() throws IOException {
        Optional<Method> method = ReflectUtils.findMethod(OkHttpClientInterceptor.class, "covertRequest",
                new Class[] {URI.class, Map.class, Request.class, String.class, ServiceInstance.class});
        Request request = createRequest(url);
        URI uri = request.uri();
        Map<String, String> hostAndPath = new HashMap<>();
        hostAndPath.put(HttpConstants.HTTP_URI_PATH, "/sayHello");
        ServiceInstance serviceInstance = new DefaultServiceInstance("127.0.0.1", "127.0.0.1", 8010,
        new HashMap<>(), "zookeeper-provider-demo");
        if (method.isPresent()) {
            Optional<Object> requestNew = ReflectUtils
                    .invokeMethod(interceptor, method.get(), new Object[] {uri, hostAndPath, request, "GET", serviceInstance});
            Assert.assertEquals(convertUrl, ((Request)requestNew.get()).uri().toString());
        }
    }

    @Test
    public void buildErrorResponseTest() throws IOException {
        Optional<Method> method = ReflectUtils.findMethod(OkHttpClientInterceptor.class, "buildErrorResponse",
                new Class[] {Exception.class, Request.class});
        Request request = createRequest(url);
        Exception ex = new Exception("error");
        if (method.isPresent()) {
            Optional<Object> response = ReflectUtils
                    .invokeMethod(interceptor, method.get(), new Object[] {ex, request});
            Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, ((Response)response.get()).code());
        }
    }
}
