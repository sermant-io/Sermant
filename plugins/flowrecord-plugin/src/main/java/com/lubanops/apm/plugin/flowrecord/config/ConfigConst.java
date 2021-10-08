/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.config;

/**
 * 配置常量类
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */
public class ConfigConst {
    /**
     * 开发环境配置文件，默认为develop
     */
    public static String CONFIG_PROFILE_ACTIVE = "develop";

    /**
     * kafka配置参数 连接服务端地址
     */
    public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

    /**
     * kafka配置参数 key序列化
     */
    public static final String KAFKA_KEY_SERIALIZER = "kafka.key.serializer";

    /**
     * kafka配置参数 value序列化
     */
    public static final String KAFKA_VALUE_SERIALIZER = "kafka.value.serializer";

    /**
     * kafka配置参数 request的topic名称
     */
    public static final String KAFKA_REQUEST_TOPIC = "kafka.request.topic";

    /**
     * kafka配置参数 producer需要server接收到数据之后发出的确认接收的信号 ack 0,1,all
     */
    public static final String KAFKA_ACKS = "kafka.acks";

    /**
     * kafka配置参数 控制生产者发送请求最大大小,默认1M （这个参数和Kafka主机的message.max.bytes 参数有关系）
     */
    public static final String KAFKA_MAX_REQUEST_SIZE = "kafka.max.request.size";

    /**
     * kafka配置参数 生产者内存缓冲区大小
     */
    public static final String KAFKA_BUFFER_MEMORY = "kafka.buffer.memory";

    /**
     * kafka配置参数 重发消息次数
     */
    public static final String KAFKA_RETRIES = "kafka.retries";

    /**
     * kafka配置参数 客户端将等待请求的响应的最大时间
     */
    public static final String KAFKA_REQUEST_TIMEOUT_MS = "kafka.request.timeout.ms";

    /**
     * kafka配置参数 最大阻塞时间，超过则抛出异常
     */
    public static final String KAFKA_MAX_BLOCK_MS = "kafka.max.block.ms";

    /**
     * 配置中心 zookeeper地址
     */
    public static final String CONFIG_ZOOKEEPER_ADDRESS = "config.zookeeper.address";

    /**
     * 配置中心 zookeeper路径
     */
    public static final String CONFIG_ZOOKEEPER_PATH = "config.zookeeper.path";

    /**
     * 配置中心 zookeeper超时时间
     */
    public static final String CONFIG_ZOOKEEPER_TIMEOUT = "config.zookeeper.timeout";

    /**
     * redis ip地址
     */
    public static final String REDIS_HOST = "redis.host";

    /**
     * redis端口
     */
    public static final String REDIS_PORT = "redis.port";

    /**
     * redis topic
     */
    public static final String REDIS_NAME = "recordjob";

    /**
     * dubbp app type
     */
    public static final String DUBBO_APP_TYPE = "Dubbo";

    /**
     * mysql app type
     */
    public static final String MYSQL_APP_TYPE = "Mysql";

    /**
     * redisson app type
     */
    public static final String REDISSON_APP_TYPE = "Redisson";

    /**
     * 自定义应用类型
     */
    public static final String CUSTOM_APP_TYPE = "Custom";

    /**
     * recordjob current job
     */
    public static final String CURRENT_JOB = "current_job";

    /**
     * record list
     */
    public static final String RECORD_LIST = "recordJobList";

    /**
     * 自定义增强类
     */
    public static final String CUSTOM_ENHANCE_CLASS = "custom.enhance.class";

    /**
     * 自定义增强的实例方法
     */
    public static final String CUSTOM_ENHANCE_INSTANCE_METHOD = "custom.enhance.instance.method";

    /**
     * 自定义增强的静态方法
     */
    public static final String CUSTOM_ENHANCE_STATIC_METHOD = "custom.enhance.static.method";
}

