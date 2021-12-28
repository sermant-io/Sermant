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

package com.huawei.hercules.util;

import org.springframework.util.StringUtils;

/**
 * 功能描述：处理mysql语句中的特殊字符
 *
 * @author zl
 * @since 2021-12-25
 */
public class MysqlCharUtil {
    /**
     * 转义mysql语句中like语句中的特殊字符
     *
     * @param original 原字符
     * @return 转义之后的字符
     */
    public static String escapeSpecialChar(String original) {
        if (StringUtils.isEmpty(original)) {
            return "";
        }
        return original.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
