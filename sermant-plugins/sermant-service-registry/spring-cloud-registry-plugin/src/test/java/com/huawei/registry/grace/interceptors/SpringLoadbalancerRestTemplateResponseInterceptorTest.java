/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.net.URI;
import java.util.Collections;

/**
 * Test RestTemplate
 *
 * @author zhouss
 * @since 2022-07-01
 */
public class SpringLoadbalancerRestTemplateResponseInterceptorTest extends ResponseTest {
    /**
     * Test rest
     *
     * @throws NoSuchMethodException Won't be thrown
     */
    @Test
    public void testRestTemplate() throws NoSuchMethodException {
        final HttpRequest request = buildRequest();
        final Object[] arguments = {request};
        final ExecuteContext executeContext = ExecuteContext
                .forMemberMethod(this, this.getClass().getDeclaredMethod("testRestTemplate"), arguments, null,
                        null);
        final SpringLoadbalancerRestTemplateResponseInterceptor interceptor =
                new SpringLoadbalancerRestTemplateResponseInterceptor();
        final ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT, Collections.singletonList(SHUTDOWN_ENDPOINT));
        Mockito.when(response.getHeaders()).thenReturn(httpHeaders);
        executeContext.changeResult(response);
        interceptor.before(executeContext);
        interceptor.after(executeContext);
        Assert.assertFalse(request.getHeaders().isEmpty());
        Assert.assertTrue(GraceContext.INSTANCE.getGraceShutDownManager().isMarkedOffline(SHUTDOWN_ENDPOINT));
    }

    private HttpRequest buildRequest() {
        return new HttpRequest() {
            private final HttpHeaders headers = new HttpHeaders();

            @Override
            public String getMethodValue() {
                return "test";
            }

            @Override
            public URI getURI() {
                return URI.create("http://localhost:8129");
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }
}
