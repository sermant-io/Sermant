/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.interceptors.http.client.httpclient.HttpClient4xInterceptor;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试HttpClient4xInterceptor
 *
 * @author tangle
 * @since 2023-07-27
 */
public class HttpClient4XInterceptorInterceptorTest extends BaseInterceptorTest {
    private final HttpClient4xInterceptor interceptor;

    private final Object[] arguments;

    public HttpClient4XInterceptorInterceptorTest() {
        interceptor = new HttpClient4xInterceptor();
        arguments = new Object[2];
    }

    @Test
    public void testClient() {
        ExecuteContext context;
        ExecuteContext resContext;
        Map<String, List<String>> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // 无Headers无Tags
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(((HttpRequest) resContext.getArguments()[1]).getAllHeaders().length, 0);

        // 有Headers无Tags
        addHeaders.put("defaultKey", Collections.singletonList("defaultValue"));
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(((HttpRequest) resContext.getArguments()[1]).getAllHeaders().length, 1);
        Assert.assertEquals(((HttpRequest) resContext.getArguments()[1]).getAllHeaders()[0].getValue(), "defaultValue");

        // 有Headers有Tags
        List<String> ids = new ArrayList<>();
        ids.add("testId001");
        ids.add("testId002");
        tags.put("id", ids);
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(((HttpRequest) resContext.getArguments()[1]).getHeaders("id").length, 2);
        Assert.assertEquals(((HttpRequest) resContext.getArguments()[1]).getHeaders("id")[0].getValue(), "testId001");

        // 测试TagTransmissionConfig开关关闭时
        tagTransmissionConfig.setEnabled(false);
        resContext = interceptor.before(context);
        Assert.assertEquals(context, resContext);
    }

    private ExecuteContext buildContext(Map<String, List<String>> addHeaders) {
        HttpRequestBase httpRequestBase = new HttpRequestBase() {
            @Override
            public String getMethod() {
                return null;
            }
        };
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                httpRequestBase.addHeader(entry.getKey(), val);
            }
        }
        arguments[1] = httpRequestBase;
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}
