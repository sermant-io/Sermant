/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.utils;

/**
 * StringUtils
 *
 * @author luanwenfei
 * @since 2022-03-24
 */
public class StringUtils {
    /**
     * Empty string.
     */
    public static final String EMPTY = "";

    private static final int FLAG = -1;

    private StringUtils() {
    }

    /**
     * equals
     *
     * @param str1 str1
     * @param str2 str2
     * @return boolean
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * equalsIgnoreCase
     *
     * @param str1 str1
     * @param str2 str2
     * @return boolean
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    /**
     * isEmpty
     *
     * @param charSequence charSequence
     * @return boolean
     */
    public static boolean isEmpty(final CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * isBlank
     *
     * @param charSequence charSequence
     * @return boolean
     */
    public static boolean isBlank(final CharSequence charSequence) {
        int charSequenceLen = charSequence == null ? 0 : charSequence.length();
        if (charSequenceLen == 0) {
            return true;
        }
        for (int i = 0; i < charSequenceLen; i++) {
            if (!Character.isWhitespace(charSequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Any string is blank.
     *
     * @param strings string list
     * @return boolean
     */
    public static boolean isAnyBlank(String... strings) {
        if (strings.length == 0) {
            return false;
        }
        for (String string : strings) {
            if (isBlank(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * none blank
     *
     * @param strings string list
     * @return boolean
     */
    public static boolean isNoneBlank(String... strings) {
        return !isAnyBlank(strings);
    }

    /**
     * isNotBlank
     *
     * @param str str
     * @return boolean
     */
    public static boolean isExist(String str) {
        return !isBlank(str);
    }

    /**
     * Wildcard matching, support '*' and '?', '*' matches any character, '?'matches one character
     *
     * @param str string
     * @param wc wildcard format
     * @return match result
     */
    public static boolean isWildcardMatch(String str, String wc) {
        final char[] strArr = str.toCharArray();
        final char[] wcArr = wc.toCharArray();
        int wcCursor = 0;
        for (int strCursor = 0, starIdx = FLAG, starCursor = 0; strCursor < strArr.length; ) {
            if (wcCursor < wcArr.length && wcArr[wcCursor] != '*'
                    && (wcArr[wcCursor] == '?' || strArr[strCursor] == wcArr[wcCursor])) {
                strCursor++;
                wcCursor++;
            } else if (wcCursor < wcArr.length && wcArr[wcCursor] == '*') {
                starIdx = wcCursor;
                starCursor = strCursor;
                wcCursor++;
            } else if (starIdx >= 0) {
                starCursor++;
                wcCursor = starIdx + 1;
                strCursor = starCursor;
            } else {
                return false;
            }
        }
        for (; wcCursor < wcArr.length; wcCursor++) {
            if (wcArr[wcCursor] != '*') {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the object's toString information
     *
     * @param object object
     * @return string
     */
    public static String getString(Object object) {
        return object == null ? "" : object.toString();
    }
}
