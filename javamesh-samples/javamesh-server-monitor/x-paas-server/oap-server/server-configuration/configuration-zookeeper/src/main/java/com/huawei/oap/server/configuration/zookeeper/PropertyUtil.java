/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.oap.server.configuration.zookeeper;

import java.util.Optional;

/**
 * 环境变量提取工具类
 *
 * @author zhouss
 * @since 2021-04-13
 **/
public class PropertyUtil {
    /**
     * 获取系统环境变量
     *
     * @param key        键
     * @param defaultVal 默认值
     * @return String
     */
    public static String getSystemEnv(String key, String defaultVal) {
        String property = System.getProperty(key);
        if (isBlank(property)) {
            property = System.getenv(key);
        }
        return Optional.ofNullable(property).orElse(defaultVal);
    }

    /**
     * 获取环境变量 无该环境变量默认返回空
     *
     * @param key 建
     * @return String
     */
    public static String getSystemEnv(String key) {
        return getSystemEnv(key, null);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isBlank(String str) {
        return str == null || "".equals(str);
    }
}
