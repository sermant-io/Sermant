/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.demo.mariadb.controller;

import com.huaweicloud.sermant.database.prohibition.common.constant.DatabaseConstant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * operate mysql by static sql
 *
 * @author daizhenyu
 * @since 2024-03-12
 **/
@RequestMapping("static")
@RestController
public class MariadbStaticSqlController {
    @Value("${mysql.address}")
    private String mysqlAddress;

    @Value("${mysql.user}")
    private String user;

    @Value("${mysql.password}")
    private String password;

    /**
     * check running status
     *
     * @return running status
     */
    @RequestMapping("checkStatus")
    public String checkStatus() {
        return "ok";
    }

    /**
     * createTable
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("createTable")
    public String createTable(String table) {
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String createTableQuery =
                    "CREATE TABLE " + table + " (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255),"
                            + " age INT)";
            statement.execute(createTableQuery);
        } catch (SQLException e) {
            if (e.getMessage().contains(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    /**
     * dropTable
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("dropTable")
    public String dropTable(String table) {
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String dropTableQuery = "DROP TABLE IF EXISTS " + table;
            statement.executeUpdate(dropTableQuery);
        } catch (SQLException e) {
            if (e.getMessage().contains(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    /**
     * createIndex
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("creatIndex")
    public String createIndex(String table) {
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String createIndexQuery = "CREATE INDEX idx_name ON " + table + " (name)";
            statement.executeUpdate(createIndexQuery);
        } catch (SQLException e) {
            if (e.getMessage().contains(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    /**
     * dropIndex
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("dropIndex")
    public String dropIndex(String table) {
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String dropIndexQuery = "DROP INDEX idx_name ON " + table;
            statement.executeUpdate(dropIndexQuery);
        } catch (SQLException e) {
            if (e.getMessage().contains(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    /**
     * alterTable
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("alterTable")
    public String alterTable(String table) {
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String alterTableQuery = "ALTER TABLE " + table + " ADD COLUMN address VARCHAR(255)";
            statement.executeUpdate(alterTableQuery);
        } catch (SQLException e) {
            if (e.getMessage().contains(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    /**
     * insert
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("insert")
    public String insert(String table) {
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String insertQuery = "INSERT INTO " + table + " (name, age) VALUES ('John Doe', 25)";
            statement.executeUpdate(insertQuery);
        } catch (SQLException e) {
            if (e.getMessage().contains(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    /**
     * update
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("update")
    public String update(String table) {
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String updateQuery = "UPDATE " + table + " SET age = 26 WHERE id = 1";
            statement.executeUpdate(updateQuery);
        } catch (SQLException e) {
            if (e.getMessage().contains(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    /**
     * delete
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("delete")
    public String delete(String table) {
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String deleteQuery = "DELETE FROM " + table + " WHERE id = 1";
            statement.executeUpdate(deleteQuery);
        } catch (SQLException e) {
            if (e.getMessage().contains(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    /**
     * select
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("select")
    public int select(String table) {
        int rowCount = 0;
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            Statement statement = connection.createStatement();
            String selectQuery = "SELECT * FROM " + table;
            ResultSet resultSet = statement.executeQuery(selectQuery);
            rowCount = countRows(resultSet);
        } catch (SQLException e) {
            // ignore
        }
        return rowCount;
    }

    private int countRows(ResultSet resultSet) throws SQLException {
        int rowCount = 0;
        if (resultSet != null) {
            resultSet.last();
            rowCount = resultSet.getRow();
            resultSet.beforeFirst();
        }
        return rowCount;
    }
}