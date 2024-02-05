/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.util;

import com.huawei.metrics.common.Constants;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL解析工具类
 *
 * @author zhp
 * @since 2024-01-15
 */
public class SqlParseUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Pattern COMMAND_TYPE_PATTERN =
            Pattern.compile("^*(INSERT\\b|UPDATE\\b|DELETE\\b|CREATE\\b|ALTER\\b|DROP\\b|TRUNCATE\\b|SET\\b"
                            + "|COMMIT\\b|SHOW\\b|USE\\b|SELECT\\b)",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern TABLE_PATTERN =
            Pattern.compile("(?i)(?:CREATE\\s+INDEX\\s+\\$\\{\\w+\\}\\s+ON|SELECT\\s+\\*\\s+FROM|DROP\\s+INDEX\\s"
                    + "+\\w+\\s+ON|CREATE\\s+INDEX\\s+\\w+\\s+ON|ALTER\\s+TABLE|INSERT\\s+INTO|UPDATE|TRUNCATE\\s+TABLE"
                    + "|CREATE\\s+TABLE|DELETE\\s+FROM|DROP\\s+"
                    + "TABLE)\\s+`*(\\w+)`*", Pattern.CASE_INSENSITIVE);

    private SqlParseUtil() {
    }

    /**
     * 获取mysql的API维度信息（命令类型_表名或命令类型）
     *
     * @param sql 执行的SQL语句
     * @return API
     */
    public static String getApi(String sql) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Matcher commandTypeMatcher = COMMAND_TYPE_PATTERN.matcher(sql);
            if (commandTypeMatcher.find()) {
                stringBuilder.append(commandTypeMatcher.group());
            }
            Matcher tableMatcher = TABLE_PATTERN.matcher(sql);
            while (tableMatcher.find()) {
                String group = tableMatcher.group();
                String[] groupStr = group.split(Constants.SPACE);
                stringBuilder.append(Constants.CONNECT).append(groupStr[groupStr.length - 1]);
            }
            return stringBuilder.toString();
        } catch (UnsupportedOperationException e) {
            LOGGER.log(Level.INFO, "There is currently no table data in SQL.");
            return stringBuilder.toString();
        }
    }
}
