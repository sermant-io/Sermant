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

package com.huawei.discovery.interceptors.httpclient;

import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.config.PlugEffectWhiteBlackConstants;
import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.entity.PlugEffectStategyCache;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.retry.RetryConfig;
import com.huawei.discovery.service.InvokerService;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * http client拦截器测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class HttpClient4xInterceptorTest {
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;
    private MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;
    private MockedStatic<OperationManager> operationManagerMockedStatic;
    private final DiscoveryPluginConfig discoveryPluginConfig = new DiscoveryPluginConfig();
    private final String hostName = discoveryPluginConfig.getRealmName();
    private final String serviceName = "provider";
    private final String path = "/hello";
    private final String uri = "http://" + hostName + "/" + serviceName + path;

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        pluginServiceManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class))
                .thenReturn(new DiscoveryPluginConfig());
        pluginServiceManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LbConfig.class))
                .thenReturn(new LbConfig());
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(InvokerService.class))
                .thenReturn(new TestInvokerService());
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
                .thenReturn(new Strategy());
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
        pluginServiceManagerMockedStatic.close();
        operationManagerMockedStatic.close();
    }

    @Test
    public void doBefore() throws Exception {
        final HttpClient4xInterceptor interceptor = new HttpClient4xInterceptor();
        interceptor.ready();
        final ExecuteContext context = buildContext();
        PlugEffectStategyCache.INSTANCE.resolve(DynamicConfigEventType.CREATE, "");
        interceptor.doBefore(context);
        interceptor.onThrow(context);
        interceptor.after(context);
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        final HttpHost httpHost = Mockito.mock(HttpHost.class);
        Mockito.when(httpHost.getHostName()).thenReturn(hostName);
        final HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
        final RequestLine requestLine = Mockito.mock(RequestLine.class);
        Mockito.when(requestLine.getMethod()).thenReturn("GET");
        Mockito.when(requestLine.getUri()).thenReturn(uri);
        Mockito.when(httpRequest.getRequestLine()).thenReturn(requestLine);
        final Object[] args = {httpHost, httpRequest};
        return ExecuteContext.forMemberMethod(this, this.getClass().getDeclaredMethod("trim",
                HttpHost.class, HttpRequest.class),
                args,
                null, null);
    }

    public String trim(HttpHost httpHost, HttpRequest httpRequest) {
        return "ok";
    }

    public static class TestInvokerService implements InvokerService {

        @Override
        public Optional<Object> invoke(Function<InvokerContext, Object> invokeFunc, Function<Exception, Object> exFunc,
                String serviceName) {
            String host = "localhost";
            String ip = "127.0.0.1";
            int port = 9090;
            final Map<String, String> meta = Collections.singletonMap("zone", "region-A");
            final DefaultServiceInstance instance = new DefaultServiceInstance(host, ip, port, meta,
                    serviceName);
            final InvokerContext invokerContext = new InvokerContext();
            invokerContext.setServiceInstance(instance);
            final Object result = invokeFunc.apply(invokerContext);
            exFunc.apply(new IOException("111"));
            return Optional.of(result);
        }

        @Override
        public Optional<Object> invoke(Function<InvokerContext, Object> invokeFunc, Function<Exception, Object> exFunc,
                String serviceName, RetryConfig retryConfig) {
            return Optional.empty();
        }
    }

    public static class Strategy implements YamlConverter {
        @Override
        public <T> Optional<T> convert(String source, Class<? super T> type) {
            return Optional.of((T) Collections.singletonMap(PlugEffectWhiteBlackConstants.DYNAMIC_CONFIG_STRATEGY,
                    PlugEffectWhiteBlackConstants.STRATEGY_ALL));
        }

        @Override
        public <T> Optional<T> convert(Reader reader, Class<? super T> type) {
            return Optional.empty();
        }

        @Override
        public String dump(Object data) {
            return null;
        }
    }
}
