package com.huawei.apm.backend.service;

import com.huawei.apm.backend.common.conf.KafkaConf;
import com.huawei.apm.backend.kafka.KafkaProducerManager;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class SendHeartbeat implements SendService{

    @Override
    public void send(KafkaConf conf, String str) {
        KafkaProducer<String, String> producer = KafkaProducerManager.getInstance(conf).getProducer();
        producer.send(new ProducerRecord<>(conf.getTopicHeartBeat(), str));
    }
}
