/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.backend.service;

import com.huawei.javamesh.backend.common.conf.KafkaConf;
import com.huawei.javamesh.backend.kafka.KafkaProducerManager;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class SendOracleJvmMonitor implements SendService {
    @Override
    public void send(KafkaConf conf, String str) {
        KafkaProducer<String, String> producer = KafkaProducerManager.getInstance(conf).getProducer();
        producer.send(new ProducerRecord<>(conf.getTopicOracleJvmMonitor(), str));
    }
}
