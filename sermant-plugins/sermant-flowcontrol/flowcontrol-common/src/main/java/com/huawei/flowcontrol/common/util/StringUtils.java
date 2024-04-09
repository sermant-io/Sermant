/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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
 * string utility class
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class StringUtils {
    /**
     * empty
     */
    public static final String EMPTY = "";

    private StringUtils() {
    }

    /**
     * determines whether the string is empty
     *
     * @param val string
     * @return return true if the string is empty or null
     */
    public static boolean isEmpty(String val) {
        return val == null || "".equals(val.trim());
    }

    /**
     * whether two strings are equal
     *
     * @param str1 string1
     * @param str2 string2
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
     * whether two strings are equal case insensitive
     *
     * @param str1 string1
     * @param str2 string2
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
     * whether the string contains
     *
     * @param source source string
     * @param subStr string
     * @return boolean
     */
    public static boolean contains(String source, String subStr) {
        if (source == null || subStr == null) {
            return false;
        }
        return source.contains(subStr);
    }

    /**
     * whether the string ends with suffix
     *
     * @param source source string
     * @param suffix suffix
     * @return boolean
     */
    public static boolean suffix(String source, String suffix) {
        if (source == null || suffix == null) {
            return false;
        }
        return source.endsWith(suffix);
    }

    /**
     * whether the string starts with prefix
     *
     * @param source source string
     * @param prefix prefix
     * @return boolean
     */
    public static boolean prefix(String source, String prefix) {
        if (source == null || prefix == null) {
            return false;
        }
        return source.startsWith(prefix);
    }

    /**
     * remove the spaces before and after the string
     *
     * @param target target string
     * @return String
     */
    public static String trim(String target) {
        return target == null ? "" : target.trim();
    }
}
