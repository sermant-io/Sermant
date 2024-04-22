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

import com.huaweicloud.sermant.core.common.LoggerFactory;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kafka message processing interceptor, obtains the traffic tag and places it in the thread variable when obtaining
 * the message content, supports 1.x, 2.x, 3.x
 *
 * @author lilai
 * @since 2023-07-18
 */
public class KafkaConsumerRecordInterceptor extends AbstractServerInterceptor<ConsumerRecord<?, ?>> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object consumerRecordObject = context.getObject();
        if (!(consumerRecordObject instanceof ConsumerRecord)) {
            return context;
        }

        Map<String, List<String>> tagMap = extractTrafficTagFromCarrier((ConsumerRecord<?, ?>) consumerRecordObject);

        // Message queue consumers do not remove thread variables and need to set new objects each time to
        // ensure variable isolation between parent and child threads
        TrafficUtils.setTrafficTag(new TrafficTag(tagMap));

        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * Parse traffic tags from ConsumerRecord
     *
     * @param consumerRecord kafka consumer traffic label carrier
     * @return traffic tag
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(ConsumerRecord<?, ?> consumerRecord) {
        Map<String, List<String>> headerMap = convertHeaders(consumerRecord);
        Map<String, List<String>> tagMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            tagMap.put(key, headerMap.get(key));
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been extracted from kafka.",
                    new Object[]{key, headerMap.get(key)});
        }
        return tagMap;
    }

    private Map<String, List<String>> convertHeaders(ConsumerRecord<?, ?> consumerRecord) {
        Map<String, List<String>> headerMap = new HashMap<>();
        for (Header header : consumerRecord.headers()) {
            String key = header.key();
            if (header.value() == null) {
                headerMap.computeIfAbsent(key, param -> null);
                continue;
            }
            String value = new String(header.value(), StandardCharsets.UTF_8);
            headerMap.computeIfAbsent(key, param -> new ArrayList<>()).add(value);
        }
        return headerMap;
    }
}
