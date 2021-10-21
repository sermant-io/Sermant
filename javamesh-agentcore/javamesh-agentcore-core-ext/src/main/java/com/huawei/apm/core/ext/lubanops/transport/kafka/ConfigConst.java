package com.huawei.apm.core.ext.lubanops.transport.kafka;

public class ConfigConst {
    /**
     * kafka配置参数 连接服务端地址
     */
    public static final String KAFKA_BOOTSTRAP_SERVERS = "127.0.0.1:9092";

    /**
     * kafka配置参数 key序列化
     */
    public static final String KAFKA_KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    /**
     * kafka配置参数 value序列化
     */
    public static final String KAFKA_VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    /**
     * kafka配置参数 producer需要server接收到数据之后发出的确认接收的信号 ack 0,1,all
     */
    public static final String KAFKA_ACKS = "1";

    /**
     * kafka配置参数 控制生产者发送请求最大大小,默认1M （这个参数和Kafka主机的message.max.bytes 参数有关系）
     */
    public static final String KAFKA_MAX_REQUEST_SIZE = "1048576";

    /**
     * kafka配置参数 生产者内存缓冲区大小
     */
    public static final String KAFKA_BUFFER_MEMORY = "33554432";

    /**
     * kafka配置参数 重发消息次数
     */
    public static final String KAFKA_RETRIES = "0";

    /**
     * kafka配置参数 客户端将等待请求的响应的最大时间
     */
    public static final String KAFKA_REQUEST_TIMEOUT_MS = "10000";

    /**
     * kafka配置参数 最大阻塞时间，超过则抛出异常
     */
    public static final String KAFKA_MAX_BLOCK_MS = "60000";
}

