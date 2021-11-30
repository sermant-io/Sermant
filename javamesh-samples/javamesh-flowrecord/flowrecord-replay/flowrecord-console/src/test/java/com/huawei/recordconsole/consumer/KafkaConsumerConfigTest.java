package com.huawei.recordconsole.consumer;

import info.batey.kafka.unit.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaConsumerConfigTest {
    @Autowired
    RecordConsoleConsumer consumer;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Before
    public void send() {
        StringBuffer sb = new StringBuffer();
        sb.append("["
        + "{\"application\":\"node1\",\"endTime\":1644206400000,\"jobId\":\"5456afa9-fd44-4801-959e-0d35fd964fcc\",\"machineList\":[\"192.168.0.97\"],\"methodList\":[\"method1\"],\"startTime\":1581048000000,\"status\":\"PENDING\",\"timeStamp\":1616223524290,\"trigger\":true}"
        + "]");
        ProducerRecord<String, String> record = new ProducerRecord<>("request", null, sb.toString());
        kafkaTemplate.send("request", sb.toString());
    }
    @Rule
    public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule();

    @Test
    public void testStart() {
        KafkaUnit kafkaUnitServer = new KafkaUnit(2181, 9200);
        kafkaUnitServer.startup();
        String testTopic = "TestTopic";
        kafkaUnitRule.getKafkaUnit().createTopic(testTopic);
        ProducerRecord<String, String> keyedMessage = new ProducerRecord<>(testTopic, "key", "value");
        kafkaUnitServer.sendMessages(keyedMessage);
        List<String> messages = kafkaUnitRule.getKafkaUnit().readMessages(testTopic, 1);
        assertEquals(Arrays.asList("value"), messages);
        kafkaUnitServer.shutdown();
    }
}