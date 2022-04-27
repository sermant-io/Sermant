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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * HTTP请求重试逻辑测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class HttpRequestInterceptorTest {
    private static final int ARGUMENT_LEN = 3;

    private ExecuteContext context;

    private Interceptor interceptor;

    /**
     * 前置初始化
     *
     * @throws Exception 初始化失败抛出
     */
    @Before
    public void before() throws Exception {
        interceptor = new HttpRequestInterceptor();
        Object[] allArguments = new Object[ARGUMENT_LEN];
        allArguments[0] = createHttpRequest();
        allArguments[1] = "test".getBytes(StandardCharsets.UTF_8);
        allArguments[2] = createExecutor();
        Object proxy = (ClientHttpRequestInterceptor) (request, body, execution) -> Mockito
            .mock(ClientHttpResponse.class);
        context = ExecuteContext.forMemberMethod(
            proxy,
            proxy.getClass().getDeclaredMethod("intercept", HttpRequest.class, byte[].class,
                ClientHttpRequestExecution.class),
            allArguments,
            Collections.emptyMap(),
            Collections.emptyMap());
    }

    /**
     * 测试流程
     *
     * @throws Exception 执行失败抛出
     */
    @Test
    public void test() throws Exception {
        interceptor.before(context);
        context.changeResult(createClientResponse());
        interceptor.after(context);
        Assert.assertNotNull(context.getResult());
        interceptor.onThrow(context);
    }

    private ClientHttpRequestExecution createExecutor() {
        return (request, body) -> createClientResponse();
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

    private HttpRequest createHttpRequest() {
        return new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.GET;
            }

            @Override
            public URI getURI() {
                return URI.create("http://localhost:9999");
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
    }
}
