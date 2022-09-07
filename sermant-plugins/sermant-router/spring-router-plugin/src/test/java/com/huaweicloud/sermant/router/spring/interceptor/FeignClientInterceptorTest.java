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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.router.spring.cache.RequestData;
import com.huaweicloud.sermant.router.spring.cache.RequestHeader;
import com.huaweicloud.sermant.router.spring.utils.ThreadLocalUtils;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import feign.Request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试FeignClientInterceptor
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class FeignClientInterceptorTest {
    private final FeignClientInterceptor interceptor;

    private final ExecuteContext context;

    public FeignClientInterceptorTest() {
        interceptor = new FeignClientInterceptor();
        Object[] arguments = new Object[1];
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("bar", Collections.singletonList("bar1"));
        headers.put("foo", Collections.singletonList("foo1"));
        arguments[0] = Request.create("GET", "/", headers, new byte[]{}, StandardCharsets.UTF_8);
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestHeader();
        ThreadLocalUtils.removeRequestData();
    }

    @Test
    public void testBeforeWithThreadLocal() {
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar2"));
        header.put("foo3", Collections.singletonList("foo3"));
        RequestHeader requestHeader = new RequestHeader(header);
        ThreadLocalUtils.setRequestHeader(requestHeader);
        interceptor.before(context);
        RequestData requestData = ThreadLocalUtils.getRequestData();
        Assert.assertNotNull(requestData);
        Assert.assertEquals(HttpMethod.GET.name(), requestData.getHttpMethod());
        Assert.assertEquals("/", requestData.getPath());
        Map<String, List<String>> headerData = requestData.getHeader();
        Assert.assertEquals(3, headerData.size());
        Assert.assertEquals("bar1", headerData.get("bar").get(0));
        Assert.assertEquals("foo1", headerData.get("foo").get(0));
        Assert.assertEquals("foo3", headerData.get("foo3").get(0));
    }

    @Test
    public void testBeforeWithHystrix() {
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar2"));
        header.put("foo3", Collections.singletonList("foo3"));
        RequestHeader requestHeader = new RequestHeader(header);
        HystrixRequestContext.initializeContext();
        HystrixRequestVariableDefault<RequestHeader> hystrixRequest = new HystrixRequestVariableDefault<>();
        hystrixRequest.set(requestHeader);
        interceptor.before(context);
        RequestData requestData = ThreadLocalUtils.getRequestData();
        Assert.assertNotNull(requestData);
        Assert.assertEquals(HttpMethod.GET.name(), requestData.getHttpMethod());
        Assert.assertEquals("/", requestData.getPath());
        Map<String, List<String>> headerData = requestData.getHeader();
        Assert.assertEquals(3, headerData.size());
        Assert.assertEquals("bar1", headerData.get("bar").get(0));
        Assert.assertEquals("foo1", headerData.get("foo").get(0));
        Assert.assertEquals("foo3", headerData.get("foo3").get(0));
    }

    @Test
    public void testBeforeWithoutHystrix() {
        interceptor.before(context);
        RequestData requestData = ThreadLocalUtils.getRequestData();
        Assert.assertNotNull(requestData);
        Assert.assertEquals(HttpMethod.GET.name(), requestData.getHttpMethod());
        Assert.assertEquals("/", requestData.getPath());
        Map<String, List<String>> headerData = requestData.getHeader();
        Assert.assertEquals(2, headerData.size());
        Assert.assertEquals("bar1", headerData.get("bar").get(0));
        Assert.assertEquals("foo1", headerData.get("foo").get(0));
    }

    @Test
    public void testBeforeWithoutRequestHeader() {
        HystrixRequestContext.initializeContext();
        HystrixRequestVariableDefault<String> hystrixRequest = new HystrixRequestVariableDefault<>();
        hystrixRequest.set("bar");
        interceptor.before(context);
        RequestData requestData = ThreadLocalUtils.getRequestData();
        Assert.assertNotNull(requestData);
        Assert.assertEquals(HttpMethod.GET.name(), requestData.getHttpMethod());
        Assert.assertEquals("/", requestData.getPath());
        Map<String, List<String>> headerData = requestData.getHeader();
        Assert.assertEquals(2, headerData.size());
        Assert.assertEquals("bar1", headerData.get("bar").get(0));
        Assert.assertEquals("foo1", headerData.get("foo").get(0));
    }

    @Test
    public void testAfter() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.after(context);
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }

    @Test
    public void testOnThrow() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }
}