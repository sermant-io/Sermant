/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.rocketmqv4.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import org.apache.rocketmq.common.protocol.header.SendMessageRequestHeader;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RocketmqProducerInterceptorTest
 *
 * @author tangle
 * @since 2023-07-27
 */
public class RocketmqProducerInterceptorTest extends BaseInterceptorTest {
    private final RocketmqProducerSendInterceptor interceptor;

    private final Object[] arguments;

    public RocketmqProducerInterceptorTest() {
        interceptor = new RocketmqProducerSendInterceptor();
        arguments = new Object[12];
    }

    @Test
    public void test() {
        ExecuteContext context;
        ExecuteContext resContext;
        Map<String, String> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // Test when the Tag Transmission Config switch is off
        tagTransmissionConfig.setEnabled(false);
        TrafficUtils.updateTrafficTag(tags);
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertNull(((SendMessageRequestHeader) resContext.getArguments()[3]).getProperties());
        tagTransmissionConfig.setEnabled(true);
        TrafficUtils.removeTrafficTag();

        // contains headers but not tags
        addHeaders.put("defualtKey", "defaultValue");
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getTruthProperties(addHeaders, new HashMap<>()),
                ((SendMessageRequestHeader) resContext.getArguments()[3]).getProperties());
        interceptor.after(context);

        // including headers also contains tags
        TrafficUtils.updateTrafficTag(tags);
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getTruthProperties(addHeaders, tags),
                ((SendMessageRequestHeader) resContext.getArguments()[3]).getProperties());
        interceptor.after(context);
        TrafficUtils.removeTrafficTag();

        // Second Message (different tag), test that tag data does not contaminate other messages
        tags.clear();
        tags.put("name", Collections.singletonList("testName001"));
        TrafficUtils.updateTrafficTag(tags);
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getTruthProperties(addHeaders, tags),
                ((SendMessageRequestHeader) resContext.getArguments()[3]).getProperties());
        interceptor.after(context);
        TrafficUtils.removeTrafficTag();
    }

    private String getTruthProperties(Map<String, String> headers, Map<String, List<String>> tags) {
        StringBuilder properties = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : tags.entrySet()) {
            properties.append(entry.getKey() + ((char) 1) + entry.getValue().get(0) + ((char) 2));
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            properties.append(entry.getKey() + ((char) 1) + entry.getValue() + ((char) 2));
        }
        if (properties.length() > 0) {
            properties.deleteCharAt(properties.length() - 1);
        }
        return properties.toString();
    }

    private ExecuteContext buildContext(Map<String, String> addHeaders) {
        SendMessageRequestHeader header = new SendMessageRequestHeader();
        StringBuilder properties = new StringBuilder();
        for (Map.Entry<String, String> entry : addHeaders.entrySet()) {
            properties.append(entry.getKey() + ((char) 1) + entry.getValue() + ((char) 2));
        }
        if (properties.length() > 0) {
            properties.deleteCharAt(properties.length() - 1);
            header.setProperties(properties.toString());
        }
        arguments[3] = header;
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}
