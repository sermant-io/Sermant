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

package com.huaweicloud.sermant.tag.transmission.interceptors.mq.kafka;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractServerInterceptor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * kafka消息处理的拦截器，在获取消息内容时获取流量标签并放置于线程变量中，支持1.x, 2.x, 3.x
 *
 * @author lilai
 * @since 2023-07-18
 */
public class KafkaConsumerRecordInterceptor extends AbstractServerInterceptor<ConsumerRecord<?, ?>> {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object consumerRecordObject = context.getObject();
        if (!(consumerRecordObject instanceof ConsumerRecord)) {
            return context;
        }

        Map<String, List<String>> tagMap = extractTrafficTagFromCarrier((ConsumerRecord<?, ?>) consumerRecordObject);

        // 消息队列消费者不会remove线程变量，需要每次set新对象，以保证父子线程之间的变量隔离
        TrafficUtils.setTrafficTag(new TrafficTag(tagMap));

        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * 从ConsumerRecord中解析流量标签
     *
     * @param consumerRecord kafka 消费端的流量标签载体
     * @return 流量标签
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(ConsumerRecord<?, ?> consumerRecord) {
        Map<String, List<String>> headerMap = convertHeaders(consumerRecord);
        Map<String, List<String>> tagMap = new HashMap<>();
        for (String key : headerMap.keySet()) {
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            tagMap.put(key, headerMap.get(key));
        }
        return tagMap;
    }

    private Map<String, List<String>> convertHeaders(ConsumerRecord<?, ?> consumerRecord) {
        Map<String, List<String>> headerMap = new HashMap<>();
        for (Header header : consumerRecord.headers()) {
            String key = header.key();
            String value = new String(header.value(), StandardCharsets.UTF_8);
            headerMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return headerMap;
    }
}
