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
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

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
public class KafkaProducerInterceptor extends AbstractClientInterceptor<ProducerRecord<?, ?>> {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object producerRecordObject = context.getArguments()[0];
        if (!(producerRecordObject instanceof ProducerRecord)) {
            return context;
        }
        injectTrafficTag2Carrier((ProducerRecord<?, ?>) producerRecordObject);
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * 向ProducerRecord中添加流量标签
     *
     * @param producerRecord kafka 标签传递载体
     */
    @Override
    protected void injectTrafficTag2Carrier(ProducerRecord<?, ?> producerRecord) {
        Headers headers = producerRecord.headers();
        for (String key : TrafficUtils.getTrafficTag().getTag().keySet()) {
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            List<String> values = TrafficUtils.getTrafficTag().getTag().get(key);
            if (CollectionUtils.isEmpty(values)) {
                continue;
            }
            for (String value : values) {
                headers.add(key, value.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
