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
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.TagTransmissionConfig;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * kafka生产者发送消息的拦截器，支持1.x, 2.x, 3.x
 *
 * @author lilai
 * @since 2023-07-18
 */
public class KafkaProducerInterceptor extends AbstractClientInterceptor {
    private final TagTransmissionConfig tagTransmissionConfig;

    /**
     * 构造器
     */
    public KafkaProducerInterceptor() {
        tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object producerRecordObject = context.getArguments()[0];
        if (producerRecordObject instanceof ProducerRecord) {
            final ProducerRecord<?, ?> producerRecord = (ProducerRecord<?, ?>) producerRecordObject;
            Headers headers = producerRecord.headers();
            for (String key : tagTransmissionConfig.getTagKeys()) {
                List<String> values = TrafficUtils.getTrafficTag().getTag().get(key);
                if (CollectionUtils.isEmpty(values)) {
                    continue;
                }
                for (String value : values) {
                    headers.add(key, value.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }
}
