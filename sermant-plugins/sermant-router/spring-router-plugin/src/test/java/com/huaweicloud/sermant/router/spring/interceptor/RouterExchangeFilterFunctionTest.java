/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.BaseTransmitConfigTest;

import reactor.core.publisher.Mono;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientResponse.Builder;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test RouterExchangeFilterFunction
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class RouterExchangeFilterFunctionTest extends BaseTransmitConfigTest {
    private final RouterExchangeFilterFunction function;

    private final ExchangeFunction exchangeFunction;

    private final ClientRequest request;

    public RouterExchangeFilterFunctionTest() {
        function = new RouterExchangeFilterFunction();
        exchangeFunction = clientRequest -> {
            Builder responseBuilder = ClientResponse.create(HttpStatus.OK);
            clientRequest.headers().forEach(
                    (key, value) -> responseBuilder.header(key, value.toArray(new String[0])));
            return Mono.just(responseBuilder.build());
        };
        request = ClientRequest.create(HttpMethod.GET, URI.create("http://127.0.0.1/foo"))
                .header("foo", "foo1").build();
    }

    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
    }

    @Test
    public void testFilterWithoutRequestTag() {
        // when requestTag is null
        ClientResponse response = function.filter(request, exchangeFunction).block();
        Assert.assertNotNull(response);
        HttpHeaders httpHeaders = response.headers().asHttpHeaders();
        Assert.assertEquals(1, httpHeaders.size());
        List<String> foos = httpHeaders.get("foo");
        Assert.assertNotNull(foos);
        Assert.assertEquals("foo1", foos.get(0));
        RequestData requestData = ThreadLocalUtils.getRequestData();
        Map<String, List<String>> tag = requestData.getTag();
        Assert.assertEquals("foo1", tag.get("foo").get(0));
        Assert.assertEquals("/foo", requestData.getPath());
        Assert.assertEquals("GET", requestData.getHttpMethod());
    }

    @Test
    public void testFilterWithRequestTag() {
        // When requestTag is not null
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("bar1")));
        ClientResponse response = function.filter(request, exchangeFunction).block();
        Assert.assertNotNull(response);
        HttpHeaders httpHeaders = response.headers().asHttpHeaders();
        Assert.assertEquals(2, httpHeaders.size());
        List<String> foos = httpHeaders.get("foo");
        Assert.assertNotNull(foos);
        Assert.assertEquals("foo1", foos.get(0));
        List<String> bars = httpHeaders.get("bar");
        Assert.assertNotNull(bars);
        Assert.assertEquals("bar1", bars.get(0));
        RequestData requestData = ThreadLocalUtils.getRequestData();
        Map<String, List<String>> tag = requestData.getTag();
        Assert.assertEquals("foo1", tag.get("foo").get(0));
        Assert.assertEquals("bar1", tag.get("bar").get(0));
        Assert.assertEquals("/foo", requestData.getPath());
        Assert.assertEquals("GET", requestData.getHttpMethod());
    }
}