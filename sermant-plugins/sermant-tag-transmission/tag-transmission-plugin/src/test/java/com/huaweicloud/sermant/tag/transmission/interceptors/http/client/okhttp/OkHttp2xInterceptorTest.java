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

package com.huaweicloud.sermant.tag.transmission.interceptors.http.client.okhttp;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.interceptors.BaseInterceptorTest;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request.Builder;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OkHttp2xInterceptor 单元测试
 *
 * @author lilai
 * @since 2023-08-17
 */
public class OkHttp2xInterceptorTest extends BaseInterceptorTest {
    private final OkHttp2xInterceptor interceptor;

    public OkHttp2xInterceptorTest() {
        this.interceptor = new OkHttp2xInterceptor();
    }

    @Test
    public void testHttpClient3() {
        ExecuteContext context;
        ExecuteContext resContext;
        Builder requestBuilder;
        Headers.Builder headerBuilder;
        List<String> nameAndValues;
        Map<String, List<String>> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // 无Headers无Tags
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        requestBuilder = (Builder) resContext.getObject();
        headerBuilder = (Headers.Builder) ReflectUtils.getFieldValue(requestBuilder, "headers").get();
        nameAndValues = (List<String>) ReflectUtils.getFieldValue(headerBuilder, "namesAndValues").get();
        Assert.assertEquals(0, nameAndValues.size());
        TrafficUtils.removeTrafficTag();

        // 有Headers无Tags
        addHeaders.put("defaultKey", Collections.singletonList("defaultValue"));
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        requestBuilder = (Builder) resContext.getObject();
        headerBuilder = (Headers.Builder) ReflectUtils.getFieldValue(requestBuilder, "headers").get();
        nameAndValues = (List<String>) ReflectUtils.getFieldValue(headerBuilder, "namesAndValues").get();
        Assert.assertEquals(2, nameAndValues.size());
        Assert.assertEquals("defaultValue", (headerBuilder.get("defaultKey")));
        TrafficUtils.removeTrafficTag();

        // 有Headers有Tags
        List<String> ids = new ArrayList<>();
        ids.add("testId001");
        ids.add("testId002");
        tags.put("id", ids);
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        requestBuilder = (Builder) resContext.getObject();
        headerBuilder = (Headers.Builder) ReflectUtils.getFieldValue(requestBuilder, "headers").get();
        nameAndValues = (List<String>) ReflectUtils.getFieldValue(headerBuilder, "namesAndValues").get();
        Assert.assertEquals(6, nameAndValues.size());
        Assert.assertEquals("testId002", headerBuilder.get("id"));
        TrafficUtils.removeTrafficTag();

        // 测试TagTransmissionConfig开关关闭时
        tagTransmissionConfig.setEnabled(false);
        addHeaders.clear();
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        requestBuilder = (Builder) resContext.getObject();
        headerBuilder = (Headers.Builder) ReflectUtils.getFieldValue(requestBuilder, "headers").get();
        nameAndValues = (List<String>) ReflectUtils.getFieldValue(headerBuilder, "namesAndValues").get();
        Assert.assertEquals(0, nameAndValues.size());
        tagTransmissionConfig.setEnabled(true);
        TrafficUtils.removeTrafficTag();
    }

    private ExecuteContext buildContext(Map<String, List<String>> addHeaders) {
        Builder builder = new Builder();
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                builder.addHeader(entry.getKey(), val);
            }
        }

        return ExecuteContext.forMemberMethod(builder, null, null, null, null);
    }
}
