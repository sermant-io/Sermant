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

package com.huawei.sermant.core.util;

public class FieldUtil {
    private static boolean isUpper(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    private static char toUpper(char ch) {
        if (isUpper(ch)) {
            return ch;
        }
        return (char) (ch + 'A' - 'a');
    }

    private static char toLower(char ch) {
        if (isUpper(ch)) {
            return (char) (ch - 'A' + 'a');
        }
        return ch;
    }

    public static String toUpperUnderline(String src) {
        return toUnderline(src, '_', true);
    }

    public static String toLowerUnderline(String src) {
        return toUnderline(src, '_', false);
    }

    public static String toUnderline(String src, boolean isUpper) {
        return toUnderline(src, '_', isUpper);
    }

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

    public static String toUpperCamel(String src) {
        return toUnderline(src, true);
    }

    public static String toLowerCamel(String src) {
        return toUnderline(src, false);
    }

    public static String toCamel(String src, boolean isUpper) {
        return toUnderline(src, '_', isUpper);
    }

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
