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

package com.huaweicloud.sermant.tag.transmission.kafka.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试KafkaConsumerRecordInterceptor
 *
 * @author tangle
 * @since 2023-07-27
 */
public class KafkaConsumerRecordInterceptorTest extends BaseInterceptorTest {
    private final KafkaConsumerRecordInterceptor interceptor;

    public KafkaConsumerRecordInterceptorTest() {
        interceptor = new KafkaConsumerRecordInterceptor();
    }

    @Test
    public void testKafkaConsumer() {
        ExecuteContext context;
        Map<String, List<String>> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // 无Headers无Tags
        context = buildContext(addHeaders, tags);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("id"));
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("name"));

        // 有Headers无Tags
        addHeaders.put("defaultKey", Collections.singletonList("defaultValue"));
        context = buildContext(addHeaders, tags);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("id"));
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("name"));

        // 有Headers有Tags
        List<String> ids = new ArrayList<>();
        ids.add("testId001");
        ids.add("testId002");
        tags.put("id", ids);
        context = buildContext(addHeaders, tags);
        interceptor.before(context);
        Assert.assertEquals(2, TrafficUtils.getTrafficTag().getTag().get("id").size());
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("name"));

        // 第二次消费，测试tags是否污染其他消费
        tags.clear();
        List<String> names = new ArrayList<>();
        names.add("testName001");
        tags.put("name", names);
        context = buildContext(addHeaders, tags);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("id"));
        Assert.assertEquals(1, TrafficUtils.getTrafficTag().getTag().get("name").size());

        // 测试TagTransmissionConfig开关关闭时
        TrafficUtils.removeTrafficTag();
        tagTransmissionConfig.setEnabled(false);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag());
    }

    private ExecuteContext buildContext(Map<String, List<String>> addHeaders, Map<String, List<String>> tags) {
        ConsumerRecord consumerRecord = new ConsumerRecord("topic", 1, 1L, "key", "value");
        Headers headers = consumerRecord.headers();
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                headers.add(entry.getKey(), val.getBytes());
            }
        }
        for (Map.Entry<String, List<String>> tag : tags.entrySet()) {
            for (String val : tag.getValue()) {
                headers.add(tag.getKey(), val.getBytes());
            }
        }
        return ExecuteContext.forMemberMethod(
                consumerRecord,
                null,
                null,
                null,
                null);
    }
}
