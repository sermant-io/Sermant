package com.huawei.javamesh.backend.service;

import com.huawei.javamesh.backend.common.conf.KafkaConf;
import com.huawei.javamesh.backend.kafka.KafkaProducerManager;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class SendHeartbeat implements SendService{

    @Override
    public void send(KafkaConf conf, String str) {
        KafkaProducer<String, String> producer = KafkaProducerManager.getInstance(conf).getProducer();
        producer.send(new ProducerRecord<>(conf.getTopicHeartBeat(), str));
    }
}
