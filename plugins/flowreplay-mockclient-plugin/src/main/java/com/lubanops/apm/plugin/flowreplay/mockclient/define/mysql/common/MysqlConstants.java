/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common;

import org.apache.skywalking.apm.agent.core.context.tag.StringTag;

/**
 * mysql插件公共常量
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class MysqlConstants {
    /**
     * CreateCallableStatementInterceptor 全限定类名
     */
    public static final String CREATE_CALLABLE_STATEMENT_INTERCEPTOR =
        "com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common.CreateCallableStatementInterceptor";
    /**
     * CreateCallableStatementInterceptor 全限定类名
     */
    public static final String CREATE_PREPARED_STATEMENT_INTERCEPTOR =
        "com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common.CreatePreparedStatementInterceptor";

    /**
     * CreateCallableStatementInterceptor 全限定类名
     */
    public static final String CREATE_STATEMENT_INTERCEPTOR =
        "com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common.CreateStatementInterceptor";

    /**
     * PreparedStatementInterceptor 全限定类名
     */
    public static final String PREPARED_STATEMENT_INTERCEPTOR =
        "com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common.PreparedStatementInterceptor";

    /**
     * SetCatalogInterceptor 全限定类名
     */
    public static final String SET_CATALOG_INTERCEPTOR =
        "com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common.SetCatalogInterceptor";

    /**
     * DriverConnectInterceptor 全限定类名
     */
    public static final String DRIVER_CONNECT_INTERCEPTOR =
        "com.huawei.apm.plugin.flowreplay.mockclient.define.mysql.common.DriverConnectInterceptor";

    /**
     * sql parameters 标签
     */
    public static final StringTag SQL_PARAMETERS = new StringTag("db.sql.parameters");

    /**
     * QueryInterceptor 全限定类名
     */
    public static final String WITNESS_MYSQL_8X_CLASS = "com.mysql.cj.interceptors.QueryInterceptor";
}
