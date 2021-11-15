/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * kafka producer 单例
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-29
 */
@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String kafkaKeySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String kafkaValueSerializer;

    /**
     * producer需要server接收到数据之后发出的确认接收的信号 ack 0,1,all
     */
    @Value("${spring.kafka.producer.acks}")
    private String kafkaAck;

    /**
     * 控制生产者发送请求最大大小,默认1M （这个参数和Kafka主机的message.max.bytes 参数有关系）
     */
    @Value("${kafka.max.request.size}")
    private String kafkaMaxRequestSize;

    /**
     * 生产者内存缓冲区大小
     */
    @Value("${spring.kafka.producer.buffer-memory}")
    private String kafkaBufferMemory;

    /**
     * 重发消息次数
     */
    @Value("${spring.kafka.producer.retries}")
    private String kafkaRetries;

    /**
     * 客户端将等待请求的响应的最大时间
     */
    @Value("${kafka.request.timeout.ms}")
    private String kafkaRequestTimeoutMs;

    /**
     * 最大阻塞时间，超过则抛出异常
     */
    @Value("${kafka.max.block.ms}")
    private String kafkaMaxBlockMs;

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaKeySerializer);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaValueSerializer);
        properties.put(ProducerConfig.ACKS_CONFIG, kafkaAck);
        properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaMaxRequestSize);
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaBufferMemory);
        properties.put(ProducerConfig.RETRIES_CONFIG, kafkaRetries);
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaRequestTimeoutMs);
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, kafkaMaxBlockMs);
        return properties;
    }

    @Bean
    public KafkaProducer<String, String> getKafkaProducer() {
        return new KafkaProducer<>(getProperties());
    }
}
