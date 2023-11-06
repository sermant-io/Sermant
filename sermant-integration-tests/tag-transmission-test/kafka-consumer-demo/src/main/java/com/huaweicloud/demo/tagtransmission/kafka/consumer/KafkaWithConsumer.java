/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.demo.tagtransmission.kafka.consumer;

import com.huaweicloud.demo.tagtransmission.midware.common.MessageConstant;
import com.huaweicloud.demo.tagtransmission.util.HttpClientUtils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * kafka消费者
 *
 * @author daizhenyu
 * @since 2023-09-08
 **/
@Component
public class KafkaWithConsumer implements CommandLineRunner {
    /**
     * 存储消费者调用http服务端返回的流量标签
     */
    public static final Map<String, String> KAFKA_TAG_MAP = new HashMap<>();

    @Value("${common.server.url}")
    private String commonServerUrl;

    @Value("${kafka.address}")
    private String kafkaAddress;

    @Override
    public void run(String[] args) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaAddress);
        properties.put("group.id", "my-group");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        consumer.subscribe(Collections.singletonList(MessageConstant.TOPIC));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(MessageConstant.KAFKA_CONSUMER_TIMEOUT);
            for (ConsumerRecord<String, String> record : records) {
                record.value();
                KAFKA_TAG_MAP.put("kafkaTag", HttpClientUtils.doHttpUrlConnectionGet(commonServerUrl));
            }
        }
    }
}
