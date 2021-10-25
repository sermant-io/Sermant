/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.kafka.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;

/**
 * @author hefan
 * @since 2021-06-21
 */
public class KafkaServiceImpl implements IKafkaService {
    private KafkaProducer<String, Bytes> producer;

    private String topic;

    public KafkaServiceImpl(KafkaProducer<String, Bytes> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public void send(SegmentObject segment) {
        ProducerRecord<String, Bytes> record = new ProducerRecord<>(
            topic,
            segment.getTraceSegmentId(),
            Bytes.wrap(segment.toByteArray())
        );
        producer.send(record);
    }
}
