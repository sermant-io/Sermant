/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.connection.pool.define;

import org.apache.skywalking.oap.server.core.oal.rt.OALDefine;

/**
 * 定义数据源的datasource，用于加载指定的oal脚本
 *
 * @author zhouss
 * @since 2020-11-28
 */
public class ConnectionPoolOALDefine extends OALDefine {
    /**
     * 控制单实例，用于初始化该模块时，进行oal脚本文件的解析
     */
    public static final ConnectionPoolOALDefine INSTANCE = new ConnectionPoolOALDefine();

    private ConnectionPoolOALDefine() {
        super("oal/connection-pool.oal", "com.huawei.apm.core.source");
    }
}
