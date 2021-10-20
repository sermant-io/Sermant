/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka消息生产工具类
 *
 */
public class KafkaProducerUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerUtil.class);
    /**
     * 使用kafka发送数据
     *
     * @param topic kafka的topic
     * @param msg 数据
     */
    public static void sendMessage(String topic, String msg) {
        KafkaProducer<String, String> producer = KafkaProducerEnum.KAFKA_PRODUCER.getKafkaProducer();


        try {
            ProducerRecord<String, String> record;
            record = new ProducerRecord<>(topic, null, msg);
            producer.send(record, (metadata, exception) -> {
                });
            } catch (Exception e) {
                LOGGER.error("[flowrecord]: send message with kafka failed");
        } finally {
            producer.flush();
        }
    }
}

