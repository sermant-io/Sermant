/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.core.alarm.config;

/**
 * kafka配置常量类
 *
 * @author hudeyu
 * @since 2021-07-27
 */
public class KafkaConst {
    protected KafkaConst() {
    }

    /**
     * 确认模式
     */
    public static final String ACKS_CONFIG = "acks";
    /**
     * 每轮批量发送的大小，单位byte
     */
    public static final String BATCH_SIZE_CONFIG = "batch.size";
    /**
     * kafka服务器地址
     */
    public static final String BOOTSTRAP_SERVERS_CONFIG = "bootstrap.servers";
    /**
     * 键序列化方式
     */
    public static final String KEY_SERIALIZER_CLASS_CONFIG = "key.serializer";
    /**
     * 每轮数据停留时间
     */
    public static final String LINGER_MS_CONFIG = "linger.ms";
    /**
     * 重试次数
     */
    public static final String RETRIES_CONFIG = "retries";
    /**
     * 值序列化方式
     */
    public static final String VALUE_SERIALIZER_CLASS_CONFIG = "value.serializer";
}
