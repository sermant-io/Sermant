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

package com.huaweicloud.sermant.core.utils;

/**
 * Field tool class
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-17
 */
public class FieldUtils {
    private FieldUtils() {
    }

    /**
     * Whether the character is uppercase
     *
     * @param ch character
     * @return Whether the value is uppercase letters
     */
    private static boolean isUpper(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    /**
     * Whether the character is lowercase
     *
     * @param ch character
     * @return Whether the value is lowercase
     */
    private static boolean isLower(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    /**
     * Converts character to uppercase
     *
     * @param ch character
     * @return Uppercase character
     */
    private static char toUpper(char ch) {
        if (isLower(ch)) {
            return (char) (ch + 'A' - 'a');
        }
        return ch;
    }

    /**
     * Converts character to lowercase
     *
     * @param ch character
     * @return Lowercase character
     */
    private static char toLower(char ch) {
        if (isUpper(ch)) {
            return (char) (ch - 'A' + 'a');
        }
        return ch;
    }

    /**
     * Camel string to uppercase underscore string
     *
     * @param src Camel string
     * @return uppercase underscore string
     */
    public static String toUpperUnderline(String src) {
        return toUnderline(src, '_', true);
    }

    /**
     * Camel string to lowercase underscore string
     *
     * @param src Camel string
     * @return lowercase underscore string
     */
    public static String toLowerUnderline(String src) {
        return toUnderline(src, '_', false);
    }

    /**
     * Camel string to underscore string
     *
     * @param src Camel string
     * @param isUpper is uppercase
     * @return underscore string
     */
    public static String toUnderline(String src, boolean isUpper) {
        return toUnderline(src, '_', isUpper);
    }

    /**
     * Camel string to underscore string
     *
     * @param src Camel string
     * @param underline underscore character
     * @param isUpper is uppercase
     * @return underscore string
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
     * Underscore string to upper camel string
     *
     * @param src Underscore string
     * @return upper camel string
     */
    public static String toUpperCamel(String src) {
        return toUnderline(src, true);
    }

    /**
     * Underscore string to lower camel string
     *
     * @param src Underscore string
     * @return lower camel string
     */
    public static String toLowerCamel(String src) {
        return toUnderline(src, false);
    }

    /**
     * Underscore string to camel string
     *
     * @param src Underscore string
     * @param isUpper upper camel or lower camel
     * @return camel string
     */
    public static String toCamel(String src, boolean isUpper) {
        return toUnderline(src, '_', isUpper);
    }

    /**
     * Underscore string to camel string
     *
     * @param src Underscore string
     * @param underline Underscore character
     * @param isUpper upper camel or lower camel
     * @return camel string
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
