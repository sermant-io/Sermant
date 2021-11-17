/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.util;

/**
 * 字符串工具类
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class StringUtils {

    /**
     * 判断字符串是否为空
     *
     * @param val 字符串
     * @return 当字符串为空或者未空串返回true
     */
    public static boolean isEmpty(String val) {
        return val == null || "".equals(val.trim());
    }
}
