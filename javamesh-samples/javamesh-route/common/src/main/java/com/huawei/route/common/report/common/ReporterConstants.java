/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.common;

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
     * ldc的配置内容
     */
    public static final String VALUE_KEY = "value";

    /**
     * ldc名称的key
     */
    public static final String LDC_KEY = "ldc";

    /**
     * 是否为网关
     */
    public static final String IS_GATE_WAY_KEY = "isGateWay";

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
     * 标签库配置的ldc的key
     */
    public static final String LDC_CONFIGURATION_KEY = "LDC_CONFIGURATION";

    /**
     * 标签库配置的灰度配置
     */
    public static final String GRAY_CONFIGURATION_KEY = "GRAY_CONFIGURATION";
}
