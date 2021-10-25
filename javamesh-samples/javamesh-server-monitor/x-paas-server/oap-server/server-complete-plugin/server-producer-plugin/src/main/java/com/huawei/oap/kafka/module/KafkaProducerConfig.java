/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.kafka.module;

import lombok.Data;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;

/**
 * kafka配置类
 *
 * @author hefan
 * @since 2021-06-21
 */
@Data
public class KafkaProducerConfig extends ModuleConfig {
    /**
     * 服务器地址
     */
    private String bootstrapServers;

    /**
     * 分区数
     */
    private int partitions;

    /**
     * 副本数
     */
    private int replicationFactor;

    /**
     * 每轮批量发送的发小，单位byte
     */
    private int batchSize;

    /**
     * 每轮数据停留时间
     */
    private long lingerMs;

    /**
     * 键序列化方式
     */
    private String keySerializer;

    /**
     * 值序列化方式
     */
    private String valueSerializer;

    /**
     * 重试次数
     */
    private int retries;

    /**
     * 确认模式
     */
    private String acks;

    /**
     * 生成者id
     */
    private String clientId;

    /**
     * 主题
     */
    private String topic;
}
