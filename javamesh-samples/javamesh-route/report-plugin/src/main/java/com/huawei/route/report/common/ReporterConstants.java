/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.common;

/**
 * 路由上报常量
 *
 * @author wl
 * @since 2021-06-16
 */
public class ReporterConstants {
    private ReporterConstants() {
    }

    /**
     * 路由服务端server的地址,默认值
     */
    public static final String DEFAULT_TARGET_SERVER_URL = "http://localhost:8010/getAddrList";

    /**
     * 路由服务端server的错误上报地址,默认值
     */
    public static final String DEFAULT_REPORT_SERVER_URL = "http://localhost:8010/reportException";

    /**
     * ldc的配置内容
     */
    public static final String VALUE_KEY = "value";

    /**
     * ldc名称的key
     */
    public static final String LDC_KEY = "ldc";

    /**
     * businesses配置的key
     */
    public static final String BUSINESS_KEY = "businesses";

    /**
     * 标签中ldc信息是否生效的key
     */
    public static final String IS_VALID_KEY = "isValid";

    /**
     * 标签是否生效的key
     */
    public static final String ON_KEY = "on";

    /**
     * 标签名称的key
     */
    public static final String LABEL_NAME_KEY = "labelName";
}
