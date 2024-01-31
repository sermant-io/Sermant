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
import com.huaweicloud.sermant.core.utils.StringUtils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQL解析工具类
 *
 * @author zhp
 * @since 2024-01-15
 */
public class SqlParseUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String STATEMENT_CLASS_NAME_SUFFIX = "Statement";

    private static final String STATEMENT_CLASS_NAME_PREFIX = "Plain";

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
            Statement statement = CCJSqlParserUtil.parse(sql);
            String className = statement.getClass().getSimpleName();
            String commandType = className.replace(STATEMENT_CLASS_NAME_PREFIX, StringUtils.EMPTY)
                    .replace(STATEMENT_CLASS_NAME_SUFFIX, StringUtils.EMPTY)
                    .toLowerCase(Locale.ROOT);
            stringBuilder.append(commandType);
            MysqlTablesNameFinder tablesNamesFinder = new MysqlTablesNameFinder();
            Set<String> tables = tablesNamesFinder.getTables(statement);
            if (tables != null) {
                for (String table : tables) {
                    stringBuilder.append(Constants.CONNECT).append(table.toLowerCase(Locale.ROOT));
                }
            }
            return stringBuilder.toString();
        } catch (JSQLParserException | UnsupportedOperationException e) {
            LOGGER.log(Level.INFO, "There is currently no table data in SQL.");
            return stringBuilder.toString();
        }
    }
}
