/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.recordconsole.netty.kafka;

import com.huawei.recordconsole.netty.common.conf.KafkaConf;
import com.huawei.recordconsole.netty.common.constants.ProducerConstants;
import com.huawei.recordconsole.netty.common.exception.KafkaTopicException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * kafka生产者管理类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-07-12
 */
public class KafkaProducerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerManager.class);

    private static volatile KafkaProducerManager INSTANCE;

    private KafkaProducer<String, Bytes> producer;

    private KafkaProducerManager(KafkaConf conf) {
        setProducerConf(conf);
    }

    /**
     * 获取kafka生产者管理类实例
     *
     * @param conf kafka配置
     * @return kafka生产者管理实例
     */
    public static KafkaProducerManager getInstance(KafkaConf conf) {
        if (INSTANCE == null) {
            synchronized (KafkaProducerManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new KafkaProducerManager(conf);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 配置kafkaConf
     *
     * @param conf kafka配置信息
     * @throws KafkaTopicException kafka主题异常
     */
    private void setProducerConf(KafkaConf conf) throws KafkaTopicException {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, conf.getBootStrapServers());
        ProducerConstants.PRODUCER_CONFIG.forEach((k, v) -> properties.setProperty(k, v));
        delKey(properties);
        producer = new KafkaProducer<>(properties, new StringSerializer(), new BytesSerializer());
    }

    private void delKey(Properties properties) {
        if (!ProducerConstants.KAFKA_IS_SSL) {
            properties.remove(ProducerConstants.KAFKA_JAAS_CONFIG_CONST);
            properties.remove(ProducerConstants.KAFKA_SASL_MECHANISM_CONST);
            properties.remove(ProducerConstants.KAFKA_SECURITY_PROTOCOL_CONST);
            properties.remove(ProducerConstants.KAFKA_SSL_TRUSTSTORE_LOCATION_CONST);
            properties.remove(ProducerConstants.KAFKA_SSL_TRUSTSTORE_PASSWORD_CONST);
            properties.remove(ProducerConstants.KAFKA_SSL_IDENTIFICATION_ALGORITHM_CONST);
        }
    }

    /**
     * 获取生产者
     *
     * @return 生产者
     */
    public KafkaProducer<String, Bytes> getProducer() {
        return producer;
    }
}
