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

package com.huawei.discovery.entity;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 * http async client上下文测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class HttpAsyncContextTest {
    @Test
    public void test() {
        final HttpAsyncContext asyncContext = new HttpAsyncContext();
        String originHostName = "originName";
        final Object handler = new Object();
        final ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        final Object callback = new Object();
        final Map<String, String> hostAndPath = Collections.emptyMap();
        String method = "POST";
        final URI uri = URI.create("http://www.xxx.com/test");
        asyncContext.setOriginHostName(originHostName);
        asyncContext.setHandler(handler);
        asyncContext.setSelectedInstance(serviceInstance);
        asyncContext.setCallback(callback);
        asyncContext.setHostAndPath(hostAndPath);
        asyncContext.setMethod(method);
        asyncContext.setUri(uri);
        Assert.assertEquals(asyncContext.getHandler(), handler);
        Assert.assertEquals(asyncContext.getOriginHostName(), originHostName);
        Assert.assertEquals(asyncContext.getSelectedInstance(), serviceInstance);
        Assert.assertEquals(asyncContext.getCallback(), callback);
        Assert.assertEquals(asyncContext.getHostAndPath(), hostAndPath);
        Assert.assertEquals(asyncContext.getMethod(), method);
        Assert.assertEquals(asyncContext.getUri(), uri);
    }
}
