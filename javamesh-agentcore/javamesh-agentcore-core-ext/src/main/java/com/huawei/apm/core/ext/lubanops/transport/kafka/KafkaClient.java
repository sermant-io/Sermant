package com.huawei.apm.core.ext.lubanops.transport.kafka;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaClient {
    private static final Logger LOGGER = LogFactory.getLogger();

    public static void sendMessage(String topic, String msg) {
        KafkaProducer<String, String> producer = KafkaProducerEnum.KAFKA_PRODUCER.getKafkaProducer();


        try {
            ProducerRecord<String, String> record;
            record = new ProducerRecord<>(topic, null, msg);
            producer.send(record, (metadata, exception) -> {
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[flowrecord]: send message with kafka failed");
        } finally {
            producer.flush();
        }
    }
}
