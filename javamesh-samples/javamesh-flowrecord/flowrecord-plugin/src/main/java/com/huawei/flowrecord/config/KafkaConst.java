/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.config;

import com.huawei.flowrecord.utils.PluginConfigUtil;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

/**
 * kafka配置
 */
public class KafkaConst {
    /**
     * 生产者配置
     *
     * @return Properties
     */
    public static Properties producerConfig() {
        FlowRecordConfig flowRecordConfig = PluginConfigUtil.getFlowRecordConfig();
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                flowRecordConfig.getKafkaBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                flowRecordConfig.getKafkaKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                flowRecordConfig.getKafkaValueSerializer());

        // producer需要server接收到数据之后发出的确认接收的信号 ack 0,1,all
        properties.put(ProducerConfig.ACKS_CONFIG,
                flowRecordConfig.getKafkaAcks());

        // 控制生产者发送请求最大大小,默认1M （这个参数和Kafka主机的message.max.bytes 参数有关系）
        properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG,
                flowRecordConfig.getKafkaMaxRequestSize());

        // 生产者内存缓冲区大小
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG,
                flowRecordConfig.getKafkaBufferMemory());

        // 重发消息次数
        properties.put(ProducerConfig.RETRIES_CONFIG,
                flowRecordConfig.getKafkaRetries());

        // 客户端将等待请求的响应的最大时间
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                flowRecordConfig.getKafkaRequestTimeoutMs());

        // 最大阻塞时间，超过则抛出异常
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG,
                flowRecordConfig.getKafkaMaxBlockMs());

        return properties;
    }
}
