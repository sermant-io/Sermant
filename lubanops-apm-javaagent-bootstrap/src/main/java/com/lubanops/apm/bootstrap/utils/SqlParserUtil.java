package com.lubanops.apm.bootstrap.utils;

import java.util.logging.Level;

import com.lubanops.apm.bootstrap.log.LogFactory;

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
