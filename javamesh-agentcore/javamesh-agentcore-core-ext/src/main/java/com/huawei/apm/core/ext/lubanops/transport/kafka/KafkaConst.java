package com.huawei.apm.core.ext.lubanops.transport.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;

public class KafkaConst {
    public static Properties producerConfig() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                ConfigConst.KAFKA_BOOTSTRAP_SERVERS);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                ConfigConst.KAFKA_KEY_SERIALIZER);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                ConfigConst.KAFKA_VALUE_SERIALIZER);

        // producer需要server接收到数据之后发出的确认接收的信号 ack 0,1,all
        properties.put(ProducerConfig.ACKS_CONFIG,
                ConfigConst.KAFKA_ACKS);

        // 控制生产者发送请求最大大小,默认1M （这个参数和Kafka主机的message.max.bytes 参数有关系）
        properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG,
                ConfigConst.KAFKA_MAX_REQUEST_SIZE);

        // 生产者内存缓冲区大小
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG,
                ConfigConst.KAFKA_BUFFER_MEMORY);

        // 重发消息次数
        properties.put(ProducerConfig.RETRIES_CONFIG,
                ConfigConst.KAFKA_RETRIES);

        // 客户端将等待请求的响应的最大时间
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                ConfigConst.KAFKA_REQUEST_TIMEOUT_MS);

        // 最大阻塞时间，超过则抛出异常
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG,
                ConfigConst.KAFKA_MAX_BLOCK_MS);

        return properties;
    }
}
