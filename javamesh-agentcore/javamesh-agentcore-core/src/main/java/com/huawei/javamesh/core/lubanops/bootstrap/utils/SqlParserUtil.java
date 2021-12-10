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

package com.huawei.javamesh.core.lubanops.bootstrap.utils;

import java.util.logging.Level;

import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;

public class SqlParserUtil {

    private static DefaultSqlParser defaultSqlParser = new DefaultSqlParser();

    public static String parse(String sql) {
        if (sql == null) {
            return null;
        }

        if (sql.startsWith("XA")) { // ddb的初级版本
            if (sql.startsWith("XA START")) {
                sql = "XA START";
            } else if (sql.startsWith("XA END")) {
                sql = "XA END";
            } else if (sql.startsWith("XA PREPARE")) {
                sql = "XA PREPARE";
            } else if (sql.startsWith("XA COMMIT")) {
                sql = "XA COMMIT";
            } else {
                sql = "XA";
            }
        } else {
            try {
                // 使用sql格式化工具类
                NormalizedSql normalizedSql = defaultSqlParser.normalizedSql(sql);
                sql = normalizedSql.getNormalizedSql();
            } catch (Throwable t) {
                LogFactory.getLogger().log(Level.SEVERE, "sql解析失败：" + sql, t);
            }
        }
        return sql;
    }
}
