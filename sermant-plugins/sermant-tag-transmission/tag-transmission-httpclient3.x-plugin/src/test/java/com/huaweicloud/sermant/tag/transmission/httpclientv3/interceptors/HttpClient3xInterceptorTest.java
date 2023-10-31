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

package com.huaweicloud.sermant.tag.transmission.httpclientv3.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpClient3xInterceptor 单元测试
 *
 * @author lilai
 * @since 2023-08-17
 */
public class HttpClient3xInterceptorTest extends BaseInterceptorTest {
    private final HttpClient3xInterceptor interceptor;

    private final Object[] arguments;

    public HttpClient3xInterceptorTest() {
        this.interceptor = new HttpClient3xInterceptor();
        this.arguments = new Object[3];
    }

    @Test
    public void testHttpClient3() {
        ExecuteContext context;
        ExecuteContext resContext;
        Map<String, List<String>> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // 无Headers无Tags
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(0, ((HttpMethod) resContext.getArguments()[1]).getRequestHeaders().length);
        TrafficUtils.removeTrafficTag();

        // 有Headers无Tags
        addHeaders.put("defaultKey", Collections.singletonList("defaultValue"));
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(1, ((HttpMethod) resContext.getArguments()[1]).getRequestHeaders().length);
        Assert.assertEquals("defaultValue",
                ((HttpMethod) resContext.getArguments()[1]).getRequestHeaders()[0].getValue()
        );
        TrafficUtils.removeTrafficTag();

        // 有Headers有Tags
        List<String> ids = new ArrayList<>();
        ids.add("testId001");
        ids.add("testId002");
        tags.put("id", ids);
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(1, ((HttpMethod) resContext.getArguments()[1]).getRequestHeaders("id").length);
        Assert.assertEquals("testId001",
                ((HttpMethod) resContext.getArguments()[1]).getRequestHeaders("id")[0].getValue());
        TrafficUtils.removeTrafficTag();

        // 测试TagTransmissionConfig开关关闭时
        tagTransmissionConfig.setEnabled(false);
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(0, ((HttpMethod) resContext.getArguments()[1]).getRequestHeaders("id").length);
        tagTransmissionConfig.setEnabled(true);
        TrafficUtils.removeTrafficTag();
    }

    private ExecuteContext buildContext(Map<String, List<String>> addHeaders) {
        HttpMethod httpMethod = new GetMethod("sermant.io");
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                httpMethod.setRequestHeader(entry.getKey(), val);
            }
        }

        arguments[1] = httpMethod;
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}
