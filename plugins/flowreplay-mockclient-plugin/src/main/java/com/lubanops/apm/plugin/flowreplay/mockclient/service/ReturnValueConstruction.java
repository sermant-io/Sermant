/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.service;

import com.sun.rowset.WebRowSetImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.CharArrayReader;
import java.sql.SQLException;

/**
 * 用于处理不同的数据结构
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-31
 */
public class ReturnValueConstruction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnValueConstruction.class);

    /**
     * ResultSetImpl 全类名
     */
    private static final String RESULT_SET_CLASS = "com.mysql.cj.jdbc.result.ResultSetImpl";

    /**
     * Boolean 全类名
     */
    private static final String BOOLEAN_CLASS = "java.lang.Boolean";

    public static Object ConstructMysqlReturnValue(String className, String content) {
        if (RESULT_SET_CLASS.equals(className)) {
            try {
                WebRowSetImpl webRowSet = new WebRowSetImpl();
                CharArrayReader charArrayReader = new CharArrayReader(content.toCharArray());
                webRowSet.readXml(charArrayReader);
                return webRowSet;
            } catch (SQLException exception) {
                LOGGER.error("WebRowSet read xml error : {}", exception.getMessage());
            }
        }
        if (BOOLEAN_CLASS.equals(className)) {
            return Boolean.parseBoolean(content);
        }
        return null;
    }
}
