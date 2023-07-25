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
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.TagTransmissionConfig;

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
public class KafkaConsumerRecordInterceptor extends AbstractServerInterceptor {
    private final TagTransmissionConfig tagTransmissionConfig;

    /**
     * 构造器
     */
    public KafkaConsumerRecordInterceptor() {
        tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object consumerRecordObject = context.getObject();
        if (consumerRecordObject instanceof ConsumerRecord) {
            final ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) consumerRecordObject;
            Map<String, List<String>> tagMap = extractTagMap(consumerRecord);
            TrafficUtils.updateTrafficTag(tagMap);
        }
        return context;
    }

    private Map<String, List<String>> extractTagMap(ConsumerRecord<?, ?> consumerRecord) {
        Map<String, List<String>> headerMap = convertHeaders(consumerRecord);
        List<String> tagKeys = tagTransmissionConfig.getTagKeys();
        Map<String, List<String>> tagMap = new HashMap<>();
        for (String key : tagKeys) {
            tagMap.put(key, headerMap.get(key));
        }
        return tagMap;
    }

    private static Map<String, List<String>> convertHeaders(ConsumerRecord<?, ?> consumerRecord) {
        Map<String, List<String>> headerMap = new HashMap<>();
        for (Header header : consumerRecord.headers()) {
            String key = header.key();
            String value = new String(header.value(), StandardCharsets.UTF_8);
            headerMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return headerMap;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }
}
