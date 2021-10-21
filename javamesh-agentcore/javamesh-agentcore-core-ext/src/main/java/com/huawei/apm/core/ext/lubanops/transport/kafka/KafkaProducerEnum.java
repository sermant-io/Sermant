package com.huawei.apm.core.ext.lubanops.transport.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;

public enum KafkaProducerEnum {
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
