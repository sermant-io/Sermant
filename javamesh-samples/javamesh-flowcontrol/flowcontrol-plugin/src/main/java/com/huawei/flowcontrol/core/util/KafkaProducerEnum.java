/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.huawei.flowcontrol.core.config.KafkaConst;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.KafkaException;

/**
 * kafka生产者枚举单例
 *
 * @author Zhang Hu
 * @since 2021-01-14
 */
public enum KafkaProducerEnum {
    /**
     * 流控kafka的生产者
     */
    KAFKA_PRODUCER;

    private KafkaProducer<String, String> kafkaProducer;

    KafkaProducerEnum() {
        try {
            // 必须添加不然会加载不到KafkaProducer
            Thread.currentThread().setContextClassLoader(null);
            kafkaProducer = new KafkaProducer<String, String>(KafkaConst.producerConfig());
            RecordLog.info("[KafkaProducerUtil] Create kafka producer successful." + kafkaProducer);
        } catch (KafkaException e) {
            RecordLog.error("[KafkaProducerUtil] kafka producer exception " + e.toString());
        }
    }

    public KafkaProducer<String, String> getKafkaProducer() {
        return KafkaProducerEnum.KAFKA_PRODUCER.kafkaProducer;
    }
}
