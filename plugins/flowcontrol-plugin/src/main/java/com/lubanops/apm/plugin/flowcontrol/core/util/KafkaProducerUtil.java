/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;

/**
 * kafka消息生产工具类
 *
 * @author liyi
 * @since 2020-08-26
 */
public class KafkaProducerUtil {
    private KafkaProducerUtil() {
    }

    /**
     * 使用kafka发送流控数据和心跳数据
     *
     * @param topic kafka的topic
     * @param msg   流控和心跳数据
     */
    public static void sendMessage(String topic, String msg) {
        KafkaProducer<String, String> producer = KafkaProducerEnum.KAFKA_PRODUCER.getKafkaProducer();

        try {
            sendRecord(topic, msg, producer);
        } finally {
            producer.flush();
        }
    }

    private static void sendRecord(final String topic, String msg,
        KafkaProducer<String, String> producer) {
        ProducerRecord<String, String> record;
        try {
            record = new ProducerRecord<String, String>(topic, null, msg);

            // 异步回调通发送数据
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        RecordLog.error("[KafkaProducerUtil] kafka exception in " + topic + exception.getMessage());
                    }
                }
            });
        } catch (KafkaException e) {
            RecordLog.error("[KafkaProducerUtil] sendMessage() exception " + e);
        }
    }
}

