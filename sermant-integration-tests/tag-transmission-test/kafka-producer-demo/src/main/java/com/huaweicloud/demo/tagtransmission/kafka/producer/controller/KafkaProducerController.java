/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.demo.tagtransmission.kafka.producer.controller;

import com.huaweicloud.demo.tagtransmission.midware.common.MessageConstant;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * 消息中间件生产者controller, 用于生产消息和查询kafka透传的流量标签
 *
 * @author daizhenyu
 * @since 2023-09-28
 **/
@RestController
@RequestMapping(value = "kafkaProducer")
public class KafkaProducerController {
    @Value("${kafka.address}")
    private String kafkaAddress;

    /**
     * kafka生产一条消息
     *
     * @return string
     */
    @RequestMapping(value = "testKafkaProducer", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testKafkaProducer() {
        produceKafkaData();
        return "kafka-produce-message-success";
    }

    /**
     * 用于检测kafka生产者进程是否正常启动
     *
     * @return string
     */
    @RequestMapping(value = "checkKafkaProducerStatus", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String checkKafkaProducerStatus() {
        return "ok";
    }

    private void produceKafkaData() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaAddress);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(properties);
        String messageBody = buildMessageBody(MessageConstant.MESSAGE_BODY_KAFKA);
        ProducerRecord<String, String> record = new ProducerRecord<>(MessageConstant.TOPIC, MessageConstant.KAFKA_KEY,
                messageBody);
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
            }
        });
        producer.close();
    }

    private String buildMessageBody(String body) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(MessageConstant.TIME_FORMAT);
        String messageBody = body + dtf.format(LocalDateTime.now());
        return messageBody;
    }
}
