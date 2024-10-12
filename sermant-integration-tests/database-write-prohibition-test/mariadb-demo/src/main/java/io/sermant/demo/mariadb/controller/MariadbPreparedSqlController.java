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

package io.sermant.demo.mariadb.controller;

import io.sermant.database.prohibition.common.constant.DatabaseConstant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * operate mysql by prepared sql
 *
 * @author daizhenyu
 * @since 2024-03-12
 **/
@RequestMapping("prepared")
@RestController
public class MariadbPreparedSqlController {
    private static final int PARAM_INDEX_FIRST = 1;

    private static final int PARAM_INDEX_SECOND = 2;

    private static final int TABLE_FIELD_AGE = 25;

    @Value("${mysql.address}")
    private String mysqlAddress;

    @Value("${mysql.user}")
    private String user;

    @Value("${mysql.password}")
    private String password;

    /**
     * createTable
     *
     * @param table table name
     * @return int prohibition status code
     */
    @RequestMapping("createTable")
    public String createTable(String table) {
        String createTableQuery =
                "CREATE TABLE " + table + " (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), age INT)";
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(createTableQuery);
            statement.executeUpdate();
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
        String dropTableQuery = "DROP TABLE IF EXISTS " + table;
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(dropTableQuery);
            statement.executeUpdate();
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
        String createIndexQuery = "CREATE INDEX idx_name ON " + table + " (name)";
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(createIndexQuery);
            statement.executeUpdate();
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
        String dropIndexQuery = "DROP INDEX idx_name ON " + table;
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(dropIndexQuery);
            statement.executeUpdate();
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
        String alterTableQuery = "ALTER TABLE " + table + " ADD COLUMN address VARCHAR(255)";
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(alterTableQuery);
            statement.executeUpdate();
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
        String insertQuery = "INSERT INTO " + table + " (name, age) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(PARAM_INDEX_FIRST, "John Doe");
            statement.setInt(PARAM_INDEX_SECOND, TABLE_FIELD_AGE);
            statement.executeUpdate();
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
        String updateQuery = "UPDATE " + table + " SET age = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setInt(PARAM_INDEX_FIRST, TABLE_FIELD_AGE);
            statement.setInt(PARAM_INDEX_SECOND, 1);
            statement.executeUpdate();
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
        String deleteQuery = "DELETE FROM " + table + " WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setInt(1, 1);
            statement.executeUpdate();
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
        String selectQuery = "SELECT * FROM " + table;
        try (Connection connection = DriverManager.getConnection(mysqlAddress, user, password)) {
            PreparedStatement statement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = statement.executeQuery();
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
