package com.lubanops.apm.plugin.servermonitor.service;

/*import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;

import java.util.Properties;*/

public class KafkaSender {



    // Map<Integer, Handler> handlers; // Type : Handler
    // handlers.get(type).handle(ByteArray)

    //private KafkaProducer<String, Bytes> producer;

    public void init() {
       /* Properties producerProperties = new Properties();
        // TODO boostrap.servers config
        producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        producer = new KafkaProducer<String, Bytes>(producerProperties, new StringSerializer(), new BytesSerializer());*/
    }

    public void send() {

    }

}
