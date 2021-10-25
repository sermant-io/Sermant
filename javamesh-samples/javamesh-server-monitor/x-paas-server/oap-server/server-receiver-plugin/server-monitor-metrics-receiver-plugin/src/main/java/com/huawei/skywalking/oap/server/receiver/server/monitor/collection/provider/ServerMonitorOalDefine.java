/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.server.monitor.collection.provider;

import org.apache.skywalking.oap.server.core.oal.rt.OALDefine;

/**
 * 服务器监控信息
 *
 * @author zhengbin zhao
 * @since 2021-02-25
 */
public class ServerMonitorOalDefine extends OALDefine {
    /**
     * 控制单实例，用于初始化该模块时，进行oal脚本文件的解析
     */
    public static final ServerMonitorOalDefine INSTANCE = new ServerMonitorOalDefine();

    private ServerMonitorOalDefine() {
        super("oal/server-monitor.oal", "com.huawei.apm.core.source");
    }
}
