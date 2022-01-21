/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.util;

import java.util.Locale;

/**
 * 字符串工具类
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class StringUtils {
    private StringUtils() {
    }

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
     * 两个字符串是否相等 - 不区分大小写
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return boolean
     */
    public static boolean equalIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.toLowerCase(Locale.ENGLISH).equals(str2.toLowerCase(Locale.ENGLISH));
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

    /**
     * 去除前后空格
     *
     * @param target 目标字符串
     * @return String
     */
    public static String trim(String target) {
        return target == null ? "" : target.trim();
    }
}
