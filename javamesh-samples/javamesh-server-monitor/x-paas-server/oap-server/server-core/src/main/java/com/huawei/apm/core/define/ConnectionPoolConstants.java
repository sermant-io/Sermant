/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.define;

/**
 * 基本常量定义
 *
 * @author zhouss
 * @since 2020-12-14
 **/
public class ConnectionPoolConstants {
    /**
     * 默认初始化容量
     */
    public static final int DEFAULT_MAP_INIT_CAPACITY = 4;

    /**
     * 数据库实例字符串分隔符
     */
    public static final String SEPARATOR_DATABASE_PEER = "\\|";

    /**
     * 连接数分隔长度
     * 依次 活动连接数、可用连接数、最大连接数
     */
    public static final int CONNECTION_COUNT_LENGTH = 3;

    /**
     * 活动连接数下标
     */
    public static final int ACTIVE_COUNT_INDEX = 0;

    /**
     * 可用连接数下标
     */
    public static final int POOLING_COUNT_INDEX = 1;

    /**
     * 最大连接数下标
     */
    public static final int MAX_ACTIVE_INDEX = 2;
}
