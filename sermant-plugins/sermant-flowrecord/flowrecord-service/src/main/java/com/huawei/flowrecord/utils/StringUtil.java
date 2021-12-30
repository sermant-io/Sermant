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

package com.huawei.flowrecord.utils;

import com.huawei.flowrecord.config.CommonConst;

public class StringUtil {
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static boolean isBlank(final CharSequence cs) {
        final int strLen = cs.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static Integer getRequestStatus(String str) {
        int beginIndex = str.indexOf("(");
        int endIndex = str.indexOf(")");
        if (beginIndex>= CommonConst.ZERO && endIndex >= CommonConst.ZERO) {
            return Integer.parseInt(str.substring(beginIndex + 1, endIndex));
        }
        return CommonConst.ZERO;
    }
}
