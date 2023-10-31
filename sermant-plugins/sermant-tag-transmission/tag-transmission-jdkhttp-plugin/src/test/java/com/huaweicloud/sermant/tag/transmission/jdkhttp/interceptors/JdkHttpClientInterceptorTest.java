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

package com.huaweicloud.sermant.tag.transmission.jdkhttp.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import sun.net.www.MessageHeader;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JdkHttpClientInterceptor 单元测试
 *
 * @author lilai
 * @since 2023-08-17
 */
public class JdkHttpClientInterceptorTest extends BaseInterceptorTest {
    private final JdkHttpClientInterceptor interceptor;

    private final Object[] arguments;

    public JdkHttpClientInterceptorTest() {
        this.interceptor = new JdkHttpClientInterceptor();
        this.arguments = new Object[1];
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
        Assert.assertEquals(0, ((MessageHeader) resContext.getArguments()[0]).getHeaders().size());
        TrafficUtils.removeTrafficTag();

        // 有Headers无Tags
        addHeaders.put("defaultKey", Collections.singletonList("defaultValue"));
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(1, ((MessageHeader) resContext.getArguments()[0]).getHeaders().size());
        Assert.assertEquals("defaultValue",
                ((MessageHeader) resContext.getArguments()[0]).getHeaders().get("defaultKey").get(0));
        TrafficUtils.removeTrafficTag();

        // 有Headers有Tags
        List<String> ids = new ArrayList<>();
        ids.add("testId001");
        ids.add("testId002");
        tags.put("id", ids);
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(2, ((MessageHeader) resContext.getArguments()[0]).getHeaders().get("id").size());
        Assert.assertEquals("testId002",
                ((MessageHeader) resContext.getArguments()[0]).getHeaders().get("id").get(0));
        TrafficUtils.removeTrafficTag();

        // 测试TagTransmissionConfig开关关闭时
        tagTransmissionConfig.setEnabled(false);
        addHeaders.clear();
        context = buildContext(addHeaders);
        TrafficUtils.updateTrafficTag(tags);
        resContext = interceptor.before(context);
        Assert.assertEquals(0, ((MessageHeader) resContext.getArguments()[0]).getHeaders().size());
        tagTransmissionConfig.setEnabled(true);
        TrafficUtils.removeTrafficTag();
    }

    private ExecuteContext buildContext(Map<String, List<String>> addHeaders) {
        MessageHeader messageHeader = new MessageHeader();
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                messageHeader.add(entry.getKey(), val);
            }
        }
        arguments[0] = messageHeader;

        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}
