/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.core.utils;

/**
 * 字段名工具类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-11-17
 */
public class FieldUtils {
    private FieldUtils() {
    }

    /**
     * 判断字符是否为大写字母
     *
     * @param ch 字符
     * @return 是否为大写字母
     */
    private static boolean isUpper(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    /**
     * 判断字符是否为小写字母
     *
     * @param ch 字符
     * @return 是否为小写字母
     */
    private static boolean isLower(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    /**
     * 将字符转换为大写形式
     *
     * @param ch 字符
     * @return 大写形式字符
     */
    private static char toUpper(char ch) {
        if (isLower(ch)) {
            return (char) (ch + 'A' - 'a');
        }
        return ch;
    }

    /**
     * 将字符转换为小写形式
     *
     * @param ch 字符
     * @return 小写形式字符
     */
    private static char toLower(char ch) {
        if (isUpper(ch)) {
            return (char) (ch - 'A' + 'a');
        }
        return ch;
    }

    /**
     * 驼峰字符串转大写下划线字符串
     *
     * @param src 驼峰字符串
     * @return 大写下划线字符串
     */
    public static String toUpperUnderline(String src) {
        return toUnderline(src, '_', true);
    }

    /**
     * 驼峰字符串转小写下划线字符串
     *
     * @param src 驼峰字符串
     * @return 小写下划线字符串
     */
    public static String toLowerUnderline(String src) {
        return toUnderline(src, '_', false);
    }

    /**
     * 驼峰字符串转下划线字符串
     *
     * @param src     驼峰字符串
     * @param isUpper 是否为大写
     * @return 下划线字符串
     */
    public static String toUnderline(String src, boolean isUpper) {
        return toUnderline(src, '_', isUpper);
    }

    /**
     * 驼峰字符串转下划线字符串
     *
     * @param src       驼峰字符串
     * @param underline 下划线字符
     * @param isUpper   是否为大写
     * @return 下划线字符串
     */
    public static String toUnderline(String src, char underline, boolean isUpper) {
        final StringBuilder sb = new StringBuilder();
        char[] charArray = src.toCharArray();
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char ch = charArray[i];
            if (i != 0 && isUpper(ch)) {
                sb.append(underline).append(isUpper ? toUpper(ch) : toLower(ch));
            } else {
                sb.append(isUpper ? toUpper(ch) : toLower(ch));
            }
        }
        return sb.toString();
    }

    /**
     * 下划线字符串转大驼峰字符串
     *
     * @param src 下划线字符串
     * @return 大驼峰字符串
     */
    public static String toUpperCamel(String src) {
        return toUnderline(src, true);
    }

    /**
     * 下划线字符串转小驼峰字符串
     *
     * @param src 下划线字符串
     * @return 小驼峰字符串
     */
    public static String toLowerCamel(String src) {
        return toUnderline(src, false);
    }

    /**
     * 下划线字符串转驼峰字符串
     *
     * @param src     下划线字符串
     * @param isUpper 大驼峰或小驼峰
     * @return 驼峰字符串
     */
    public static String toCamel(String src, boolean isUpper) {
        return toUnderline(src, '_', isUpper);
    }

    /**
     * 下划线字符串转驼峰字符串
     *
     * @param src       下划线字符串
     * @param underline 下划线字符
     * @param isUpper   大驼峰或小驼峰
     * @return 驼峰字符串
     */
    public static String toCamel(String src, char underline, boolean isUpper) {
        final StringBuilder sb = new StringBuilder();
        final char[] chars = src.toCharArray();
        for (int i = 0, charsLength = chars.length; i < charsLength; i++) {
            char ch = chars[i];
            if (i == 0) {
                sb.append(isUpper ? toUpper(ch) : toLower(ch));
            } else if (ch == underline && i + 1 < chars.length) {
                sb.append(toUpper(chars[++i]));
            } else {
                sb.append(toLower(ch));
            }
        }
        return sb.toString();
    }
}
