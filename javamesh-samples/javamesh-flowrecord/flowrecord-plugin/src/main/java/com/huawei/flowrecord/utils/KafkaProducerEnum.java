/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.utils;

import com.huawei.flowrecord.config.KafkaConst;

import org.apache.kafka.clients.producer.KafkaProducer;

/**
 * kafka生产者实例
 *
 */
public enum KafkaProducerEnum {
    /**
     * kafka的生产者
     */
    KAFKA_PRODUCER,;
    private KafkaProducer<String, String> kafkaProducer;

    KafkaProducerEnum() {
        Thread.currentThread().setContextClassLoader(null);
        kafkaProducer = new KafkaProducer<>(KafkaConst.producerConfig());
    }

    public KafkaProducer<String, String> getKafkaProducer() {
        return KafkaProducerEnum.KAFKA_PRODUCER.kafkaProducer;
    }
}
