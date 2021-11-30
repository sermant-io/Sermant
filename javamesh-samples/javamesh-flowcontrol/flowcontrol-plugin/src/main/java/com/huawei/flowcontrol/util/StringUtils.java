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

    /**
     * 两个字符串是否相等
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return boolean
     */
    public static boolean equal(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 字符串是否包含
     *
     * @param source 源字符串
     * @param subStr 字符串
     * @return boolean
     */
    public static boolean contains(String source, String subStr) {
        if (source == null || subStr == null) {
            return false;
        }
        return source.contains(subStr);
    }

    /**
     * 字符串是否以suffix结尾
     *
     * @param source 源字符串
     * @param suffix 后缀
     * @return boolean
     */
    public static boolean suffix(String source, String suffix) {
        if (source == null || suffix == null) {
            return false;
        }
        return source.endsWith(suffix);
    }

    /**
     * 字符串是否以suffix结尾
     *
     * @param source 源字符串
     * @param prefix 后缀
     * @return boolean
     */
    public static boolean prefix(String source, String prefix) {
        if (source == null || prefix == null) {
            return false;
        }
        return source.startsWith(prefix);
    }
}
