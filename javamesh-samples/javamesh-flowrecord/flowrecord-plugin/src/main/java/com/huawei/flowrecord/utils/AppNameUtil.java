/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.utils;

/**
 * 获取appname使用
 *
 */
public final class AppNameUtil {
    /**
     * 获取appname
     */
    public static final String APP_NAME = "project.name";

    private static String appName;

    private AppNameUtil() {
    }

    static {
        resolveAppName();
    }

    public static void resolveAppName() {
        String app = System.getProperty(APP_NAME);

        // use -Dproject.name first
        if (!StringUtil.isEmpty(app)) {
            appName = app;
        } else {
            appName = "default";
        }
        return;
    }

    public static String getAppName() {
        return appName;
    }
}
