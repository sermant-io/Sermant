/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.retry;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.service.rest4j.HttpRest4jService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;

/**
 * HTTP request retry logic test
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class HttpRequestInterceptorTest {
    private static final int ARGUMENT_LEN = 1;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private ExecuteContext context;

    private Interceptor interceptor;

    /**
     * preinitialization
     *
     * @throws Exception initialization failure thrown
     */
    @Before
    public void before() throws Exception {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(HttpRest4jService.class))
                .thenReturn(createRestService());
        interceptor = new HttpRequestInterceptor();
        Object[] allArguments = new Object[ARGUMENT_LEN];
        AbstractClientHttpRequest proxy = Mockito.mock(AbstractClientHttpRequest.class);
        final HttpHeaders httpHeaders = new HttpHeaders();
        allArguments[0] = httpHeaders;
        Mockito.when(proxy.getHeaders()).thenReturn(httpHeaders);
        Mockito.when(proxy.getMethod()).thenReturn(HttpMethod.GET);
        Mockito.when(proxy.getURI()).thenReturn(URI.create("http://rest-provider/api"));
        context = ExecuteContext.forMemberMethod(
            proxy,
            proxy.getClass().getDeclaredMethod("executeInternal", HttpHeaders.class),
            allArguments,
            Collections.emptyMap(),
            Collections.emptyMap());
    }

    @After
    public void after() {
        pluginConfigManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
    }

    /**
     * testing process
     *
     * @throws Exception execution failure throw
     */
    @Test
    public void test() throws Exception {
        interceptor.before(context);
        context.changeResult(createClientResponse());
        interceptor.after(context);
        Assert.assertNotNull(context.getResult());
        interceptor.onThrow(context);
    }

    private HttpRest4jService createRestService() {
        return new HttpRest4jService() {
            @Override
            public void onBefore(String sourceName, RequestEntity requestEntity,
                    FlowControlResult fixedResult) {

            }

            @Override
            public void onAfter(String sourceName, Object result) {

            }

            @Override
            public void onThrow(String sourceName, Throwable throwable) {

            }
        };
    }

    private ClientHttpResponse createClientResponse() {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() {
                return HttpStatus.OK;
            }

            @Override
            public int getRawStatusCode() {
                return HttpStatus.OK.value();
            }

            @Override
            public String getStatusText() {
                return "ok";
            }

            @Override
            public void close() {
            }

            @Override
            public InputStream getBody() {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
    }
}
