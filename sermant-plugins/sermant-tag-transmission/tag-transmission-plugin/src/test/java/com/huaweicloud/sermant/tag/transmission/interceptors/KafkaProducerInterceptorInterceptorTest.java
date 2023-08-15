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
import com.huaweicloud.sermant.tag.transmission.interceptors.mq.kafka.KafkaProducerInterceptor;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 测试KafkaProducerInterceptor
 *
 * @author tangle
 * @since 2023-07-27
 */
public class KafkaProducerInterceptorInterceptorTest extends BaseInterceptorTest {
    private final KafkaProducerInterceptor interceptor;

    private final Object[] arguments;

    public KafkaProducerInterceptorInterceptorTest() {
        interceptor = new KafkaProducerInterceptor();
        arguments = new Object[2];
    }

    @Test
    public void testKafkaProducer() {
        ExecuteContext context;
        ExecuteContext resContext;
        Map<String, List<String>> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // 有Headers无Tags
        addHeaders.put("defaultKey", Collections.singletonList("defaultValue"));
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getValuesFromRecord(resContext, "defaultKey"), Collections.singletonList("defaultValue"));

        // 有Headers有Tags
        List<String> ids = new ArrayList<>();
        ids.add("testId001");
        ids.add("testId002");
        tags.put("id", ids);
        TrafficUtils.updateTrafficTag(tags);
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getValuesFromRecord(resContext, "id"), ids);

        // 第二次生产，测试tags是否污染其他消费
        tags.clear();
        List<String> names = new ArrayList<>();
        names.add("testName001");
        tags.put("name", names);
        TrafficUtils.updateTrafficTag(tags);
        context = buildContext(addHeaders);
        resContext = interceptor.before(context);
        Assert.assertEquals(getValuesFromRecord(resContext, "id"), new ArrayList<>());
        Assert.assertEquals(getValuesFromRecord(resContext, "name"), names);

        // 测试TagTransmissionConfig开关关闭时
        tagTransmissionConfig.setEnabled(false);
        resContext = interceptor.before(context);
        Assert.assertEquals(context, resContext);
    }

    private List<String> getValuesFromRecord(ExecuteContext resContext, String key) {
        Headers headers = ((ProducerRecord) resContext.getArguments()[0]).headers();
        if (headers.headers(key) == null) {
            return null;
        }
        List<String> res = new ArrayList<>();
        Iterator<Header> iterator = headers.headers(key).iterator();
        while (iterator.hasNext()) {
            Header header = iterator.next();
            byte[] bts = header.value();
            res.add(new String(bts, Charset.forName("UTF-8")));
        }
        return res;
    }

    private ExecuteContext buildContext(Map<String, List<String>> addHeaders) {
        ProducerRecord producerRecord = new ProducerRecord<>("topic", "value");
        Headers headers = producerRecord.headers();
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                headers.add(entry.getKey(), val.getBytes());
            }
        }
        arguments[0] = producerRecord;
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}
