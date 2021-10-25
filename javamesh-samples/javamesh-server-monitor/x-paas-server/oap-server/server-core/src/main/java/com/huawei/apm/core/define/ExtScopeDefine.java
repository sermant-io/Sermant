/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.define;

import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;

/**
 * ext the DefaultScopeDefine to config custom scope
 *
 * @author zhouss
 * @since 2020-11-27
 */
public class ExtScopeDefine extends DefaultScopeDefine {
    /**
     * 实例，应用，数据源 新增scope
     */
    public static final int CONNECTION_POOL_METRIC = 10001;

    /**
     * 服务器cpu
     */
    public static final int SERVER_MONITOR_CPU_METRIC = 10002;

    /**
     * 服务器磁盘IO
     */
    public static final int SERVER_MONITOR_DISK_METRIC = 10003;

    /**
     * 服务器网络IO
     */
    public static final int SERVER_MONITOR_NETWORK_METRIC = 10004;

    /**
     * 服务器物理内存
     */
    public static final int SERVER_MONITOR_MEMORY_METRIC = 10005;

    /**
     * ibm jdk
     */
    public static final int SERVICE_INSTANCE_IBM_JVM_MEMORY_POOL = 10006;
}
