/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.oap.redis.module;

import lombok.Data;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;

/**
 * redis查询配置类
 *
 * @author zhouss
 * @since 2020-11-28
 */
@Data
public class RedisSingletonConfig extends ModuleConfig {
    /**
     * redis单机节点
     */
    private String node;

    /**
     * 连接超时时间
     */
    private int timeout;

    /**
     * 数据库id
     */
    private int database;

    /**
     * 最大活动连接数
     */
    private int maxActive;

    /**
     * 最大等待时间
     */
    private int maxWait;

    /**
     * 最大空闲连接数
     */
    private int maxIdle;

    /**
     * 最小空闲连接数
     */
    private int minIdle;
}
