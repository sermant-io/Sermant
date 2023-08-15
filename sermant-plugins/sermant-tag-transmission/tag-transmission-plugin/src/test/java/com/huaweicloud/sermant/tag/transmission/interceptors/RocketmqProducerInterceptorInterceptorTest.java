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
import com.huaweicloud.sermant.tag.transmission.interceptors.mq.rocketmq.RocketmqProducerInterceptor;

import org.apache.rocketmq.common.protocol.header.SendMessageRequestHeader;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试RocketmqProducerInterceptor
 *
 * @author tangle
 * @since 2023-07-27
 */
public class RocketmqProducerInterceptorInterceptorTest extends BaseInterceptorTest {
    private final RocketmqProducerInterceptor interceptor;

    private final Object[] arguments;

    public RocketmqProducerInterceptorInterceptorTest() {
        interceptor = new RocketmqProducerInterceptor();
        arguments = new Object[12];
    }

    @Test
    public void test() {
        ExecuteContext context;
        ExecuteContext resContext;
        Map<String, String> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // 包含Headers但不包含tags
        addHeaders.put("defualtKey", "defaultValue");
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getTruthProperties(addHeaders, new HashMap<>()),
                ((SendMessageRequestHeader) resContext.getArguments()[3]).getProperties());
        interceptor.after(context);

        // 包含Headers也包含tag
        TrafficUtils.updateTrafficTag(tags);
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getTruthProperties(addHeaders, tags),
                ((SendMessageRequestHeader) resContext.getArguments()[3]).getProperties());
        interceptor.after(context);

        // 第二次Message（不同tag）,测试tag数据不会污染其他Message
        tags.clear();
        tags.put("name", Collections.singletonList("testName001"));
        TrafficUtils.updateTrafficTag(tags);
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getTruthProperties(addHeaders, tags),
                ((SendMessageRequestHeader) resContext.getArguments()[3]).getProperties());
        interceptor.after(context);

        // 测试TagTransmissionConfig开关关闭时
        tagTransmissionConfig.setEnabled(false);
        resContext = interceptor.before(context);
        Assert.assertEquals(context, resContext);
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
