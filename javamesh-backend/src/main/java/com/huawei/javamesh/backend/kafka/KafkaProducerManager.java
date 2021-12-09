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

package com.huawei.javamesh.backend.kafka;

import com.huawei.javamesh.backend.common.conf.KafkaConf;
import com.huawei.javamesh.backend.common.exception.KafkaTopicException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * kafka生产者管理类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class KafkaProducerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerManager.class);

    private KafkaProducer<String, byte[]> producer;

    private static KafkaProducerManager instance;

    private KafkaProducerManager(KafkaConf conf) {
        setProducerConf(conf);
    }

    /**
     * 获取kafka生产者管理类实例
     *
     * @param conf kafka配置
     * @return kafka生产者管理实例
     */
    public static synchronized KafkaProducerManager getInstance(KafkaConf conf) {
        if (instance == null) {
            instance = new KafkaProducerManager(conf);
        }
        return instance;
    }

    /**
     * 配置kafkaConf
     *
     * @param conf kafka配置信息
     * @throws KafkaTopicException kafka主题异常
     */
    private void setProducerConf(KafkaConf conf) throws KafkaTopicException {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, conf.getBootStrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, conf.getKafkaKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, conf.getKafkaValueSerializer());
        properties.put(ProducerConfig.ACKS_CONFIG, conf.getKafkaAcks());
        properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, conf.getKafkaMaxRequestSize());
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, conf.getKafkaBufferMemory());
        properties.put(ProducerConfig.RETRIES_CONFIG, conf.getKafkaRetries());
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, conf.getKafkaRequestTimeoutMs());
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, conf.getKafkaMaxBlockMs());
        producer = new KafkaProducer<>(properties);
    }

    /**
     * 获取生产者
     *
     * @return 生产者
     */
    public KafkaProducer<String, byte[]> getProducer() {
        return producer;
    }
}
