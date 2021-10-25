/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.source;

/**
 * 连接池类型定义
 *
 * @author zhouss
 * @since 20210301
 */
public enum ConnectionPoolType {
    DRUID,
    C3P0,
    DBCP,
    UNRECOGNIZED;

    /**
     * 转换连接池类型，以便于oal进行连接池类型过滤
     *
     * @param poolType protocol生成的连接池类型
     * @return 当前定义的连接池类型
     */
    public static ConnectionPoolType getConvertedPoolType(
            com.huawei.apm.network.language.agent.v3.ConnectionPoolType poolType) {
        switch (poolType) {
            case DRUID:
                return ConnectionPoolType.DRUID;
            case DBCP:
                return ConnectionPoolType.DBCP;
            case C3P0:
                return ConnectionPoolType.C3P0;
            default:
                return ConnectionPoolType.UNRECOGNIZED;
        }
    }
}
