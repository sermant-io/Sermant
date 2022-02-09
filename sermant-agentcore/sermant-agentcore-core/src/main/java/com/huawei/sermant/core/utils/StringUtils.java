/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
 * 字符串工具类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-18
 */
public class StringUtils {
    private StringUtils() {
    }

    /**
     * 是否通配符匹配，支持'*'和'?'，'*'匹配任意字符，'?'匹配一个字符
     *
     * @param str 字符串
     * @param wc  通配符匹配格式
     * @return 是否匹配成功
     */
    public static boolean isWildcardMatch(String str, String wc) {
        final char[] strArr = str.toCharArray();
        final char[] wcArr = wc.toCharArray();
        int wcCursor = 0;
        for (int strCursor = 0, starIdx = -1, starCursor = 0; strCursor < strArr.length; ) {
            if (wcCursor < wcArr.length && wcArr[wcCursor] != '*' && (wcArr[wcCursor] == '?'
                    || strArr[strCursor] == wcArr[wcCursor])) {
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
}
