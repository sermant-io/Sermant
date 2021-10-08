/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.common;

/**
 * Mysql constants 公共的interceptor地址
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class MysqlCommonConstants {
    /**
     * CreatePreparedStatementInterceptor 全类名
     */
    public static final String CREATE_PREPARED_STATEMENT_INTERCEPTOR
        = "com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.CreatePreparedStatementInterceptor";

    /**
     * PreparedStatementInterceptor 全类名
     */
    public static final String PREPARED_STATEMENT_INTERCEPTOR
        = "com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.PreparedStatementInterceptor";

    /**
     * SetCatalogInterceptor 全类名
     */
    public static final String SET_CATALOG_INTERCEPTOR
        = "com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.SetCatalogInterceptor";

    /**
     * DriverConnectInterceptor 全类名
     */
    public static final String DRIVER_CONNECT_INTERCEPTOR
        = "com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.DriverConnectInterceptor";

    /**
     * 用于区分不同版本的插件
     */
    public static final String WITNESS_MYSQL_5X_CLASS = "com.mysql.jdbc.ConnectionImpl";

    /**
     * 用于区分不同版本的插件
     */
    public static final String WITNESS_MYSQL_8X_CLASS = "com.mysql.cj.interceptors.QueryInterceptor";
}
