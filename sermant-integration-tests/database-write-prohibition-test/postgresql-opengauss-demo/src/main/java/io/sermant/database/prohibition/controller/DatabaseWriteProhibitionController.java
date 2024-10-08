/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.database.prohibition.controller;

import io.sermant.database.prohibition.common.constant.DatabaseConstant;
import io.sermant.database.prohibition.entity.Result;
import io.sermant.database.prohibition.entity.Students;
import io.sermant.database.prohibition.mapper.DatabaseMapper;
import io.sermant.database.prohibition.mapper.StudentsMapper;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Locale;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;

/**
 * Database write prohibition test interface
 *
 * @author zhp
 * @since 2024-01-13
 */
@RestController
public class DatabaseWriteProhibitionController implements InitializingBean {
    @Resource
    private StudentsMapper studentsMapper;

    @Resource
    private DatabaseMapper databaseMapper;

    @Value(value = "${spring.datasource.url}")
    private String url;

    @Value(value = "${spring.datasource.username}")
    private String username;

    @Value(value = "${spring.datasource.password}")
    private String password;

    private Connection connection;

    /**
     * query data based on id
     *
     * @param id table primary key
     * @return Students
     */
    @GetMapping("/getById")
    public Result getById(@PathParam("id") int id) {
        Students students = studentsMapper.selectById(id);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, students);
    }

    /**
     * save student information
     *
     * @param name student Name
     * @param age student age
     * @return number of successful insertions
     */
    @GetMapping("/saveStudents")
    public Result saveStudents(@PathParam("name") String name, @PathParam("age") int age) {
        Students students = new Students();
        students.setAge(age);
        students.setName(name);
        studentsMapper.insert(students);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, students);
    }

    /**
     * update student information based on id
     *
     * @param name student Name
     * @param id table primary key
     * @param age student age
     * @return number of successful updates
     */
    @GetMapping("/updateStudents")
    public Result updateStudents(@PathParam("name") String name, @PathParam("age") int age, @PathParam("id") int id) {
        Students students = new Students();
        students.setAge(age);
        students.setName(name);
        students.setId(id);
        studentsMapper.updateById(students);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, students);
    }

    /**
     * update student information based on id
     *
     * @param id table primary key
     * @return number of successful deletions
     */
    @GetMapping("/deleteStudents")
    public Result updateStudents(@PathParam("id") int id) {
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, studentsMapper.deleteById(id));
    }

    /**
     * create table
     *
     * @param tableName table name
     * @param sequenceName sequence name
     * @return create table results
     */
    @GetMapping("/createTable")
    public Result createTable(@PathParam("tableName") String tableName,
            @PathParam("sequenceName") String sequenceName) {
        databaseMapper.createTable(tableName, sequenceName);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }

    /**
     * delete table
     *
     * @param tableName table name
     * @return delete table results
     */
    @GetMapping("/deleteTable")
    public Result deleteTable(@PathParam("tableName") String tableName) {
        databaseMapper.deleteTable(tableName);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }

    /**
     * create index
     *
     * @param indexName index name
     * @param tableName table name
     * @param columnName column name
     * @return create index result
     */
    @GetMapping("/createIndex")
    public Result createIndex(@PathParam("tableName") String tableName, @PathParam("indexName") String indexName,
            @PathParam("columnName") String columnName) {
        databaseMapper.createIndex(indexName, tableName, columnName);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }

    /**
     * delete index
     *
     * @param indexName index name
     * @return delete index result
     */
    @GetMapping("/deleteIndex")
    public Result deleteIndex(@PathParam("indexName") String indexName) {
        databaseMapper.deleteIndex(indexName);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }

    /**
     * create sequence
     *
     * @param sequenceName sequence name
     * @return create sequence result
     */
    @GetMapping("/createSequence")
    public Result createSequence(@PathParam("sequenceName") String sequenceName) {
        databaseMapper.createSequence(sequenceName);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }

    /**
     * delete sequence
     *
     * @param sequenceName sequence name
     * @return create sequence result
     */
    @GetMapping("/deleteSequence")
    public Result deleteSequence(@PathParam("sequenceName") String sequenceName) {
        databaseMapper.deleteSequence(sequenceName);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }

    /**
     * create sequence
     *
     * @param tableName table name
     * @param columnName column name
     * @param columnType column type
     * @return alter table result
     */
    @GetMapping("/alterTable")
    public Result alterTable(@PathParam("tableName") String tableName, @PathParam("columnName") String columnName,
            @PathParam("columnType") String columnType) {
        databaseMapper.addColumn(tableName, columnName, columnType);
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }

    /**
     * query data
     *
     * @param columnValue column value
     * @param tableName table name
     * @param columnName column name
     * @return Students
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/getDataByStaticSql")
    public Result getDataByStaticSql(@PathParam("tableName") String tableName,
            @PathParam("columnName") String columnName, @PathParam("columnValue") String columnValue)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.SELECT_SQL, tableName, columnName, columnValue);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
        }
    }

    /**
     * update data
     *
     * @param tableName table name
     * @param columnNames column names
     * @param columnValues column values
     * @return updated result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/saveDataByStaticSql")
    public Result saveDataByStaticSql(@PathParam("tableName") String tableName,
            @PathParam("columnNames") String columnNames, @PathParam("columnValues") String columnValues)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.INSERT_SQL, tableName, columnNames, columnValues);
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, resultSet.getInt("id"));
            }
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
        }
    }

    /**
     * update data
     *
     * @param tableName table name
     * @param columnNames column names
     * @param columnValues column values
     * @return updated result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/updateDataByStaticSql")
    public Result updateDataByStaticSql(@PathParam("tableName") String tableName,
            @PathParam("columnNames") String columnNames, @PathParam("columnValues") String columnValues)
            throws SQLException {
        String[] columnNameArray = columnNames.split(DatabaseConstant.PARAM_SEPARATOR);
        String[] columnValueArray = columnValues.split(DatabaseConstant.PARAM_SEPARATOR);
        if (columnNameArray.length <= 1 || columnValueArray.length <= 1) {
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, "parameter format not correct", null);
        }
        String sql = String.format(Locale.ROOT, DatabaseConstant.UPDATE_SQL, tableName, columnNameArray[0],
                columnValueArray[0], columnNameArray[1], columnValueArray[1]);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * delete data
     *
     * @param tableName table name
     * @param columnNames column names
     * @param columnValues column values
     * @return updated result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/deleteDataByStaticSql")
    public Result deleteDataByStaticSql(@PathParam("tableName") String tableName,
            @PathParam("columnNames") String columnNames, @PathParam("columnValues") String columnValues)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.DELETE_SQL, tableName, columnNames, columnValues);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * create table
     *
     * @param tableName table name
     * @param sequenceName sequence name
     * @return create table result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/createTableByStaticSql")
    public Result createTableByStaticSql(@PathParam("tableName") String tableName,
            @PathParam("sequenceName") String sequenceName)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.CREATE_TABLE_SQL, tableName, sequenceName);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * delete table
     *
     * @param tableName table name
     * @return delete table result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/deleteTableByStaticSql")
    public Result deleteTableByStaticSql(@PathParam("tableName") String tableName)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.DELETE_TABLE_SQL, tableName);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * create index
     *
     * @param indexName index name
     * @param tableName table name
     * @param columnName column name
     * @return create index result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/createIndexByStaticSql")
    public Result createIndexByStaticSql(@PathParam("tableName") String tableName,
            @PathParam("indexName") String indexName, @PathParam("columnName") String columnName)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.CREATE_INDEX_SQL, indexName, tableName, columnName);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * delete index
     *
     * @param indexName index name
     * @return delete index result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/deleteIndexByStaticSql")
    public Result deleteIndexByStaticSql(@PathParam("indexName") String indexName)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.DELETE_INDEX_SQL, indexName);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * create sequence
     *
     * @param sequenceName sequence name
     * @return create sequence result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/createSequenceByStaticSql")
    public Result createSequenceByStaticSql(@PathParam("sequenceName") String sequenceName) throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.CREATE_SEQUENCE_SQL, sequenceName);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * delete sequence
     *
     * @param sequenceName sequence name
     * @return create sequence result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/deleteSequenceByStaticSql")
    public Result deleteSequenceByStaticSql(@PathParam("sequenceName") String sequenceName) throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.DELETE_SEQUENCE_SQL, sequenceName);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * alter table
     *
     * @param tableName table name
     * @param columnName column name
     * @param columnType column type
     * @return alter table result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/static/alterTable")
    public Result alterTableByStaticSql(@PathParam("tableName") String tableName,
            @PathParam("columnName") String columnName, @PathParam("columnType") String columnType)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.ALTER_TABLE_SQL, tableName, columnName, columnType);
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(sql);
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * query data
     *
     * @return Students
     */
    @GetMapping("/batch/getDataByBatch")
    public Result getDataByBatch() {
        studentsMapper.selectBatchIds(Collections.singletonList(1));
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }

    /**
     * update data
     *
     * @param tableName table name
     * @param columnNames column names
     * @param columnValues column values
     * @return updated result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/saveDataByBatch")
    public Result saveDataByBatch(@PathParam("tableName") String tableName,
            @PathParam("columnNames") String columnNames, @PathParam("columnValues") String columnValues)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.INSERT_SQL_NO_RETURN, tableName, columnNames,
                columnValues);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] result = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, result[0]);
        }
    }

    /**
     * update data
     *
     * @param tableName table name
     * @param columnNames column names
     * @param columnValues column values
     * @return updated result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/updateDataByBatch")
    public Result updateDataByBatch(@PathParam("tableName") String tableName,
            @PathParam("columnNames") String columnNames, @PathParam("columnValues") String columnValues)
            throws SQLException {
        String[] columnNameArray = columnNames.split(DatabaseConstant.PARAM_SEPARATOR);
        String[] columnValueArray = columnValues.split(DatabaseConstant.PARAM_SEPARATOR);
        String sql = String.format(Locale.ROOT, DatabaseConstant.UPDATE_SQL, tableName, columnNameArray[0],
                columnValueArray[0], columnNameArray[1], columnValueArray[1]);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * delete data
     *
     * @param tableName table name
     * @param columnNames column names
     * @param columnValues column values
     * @return updated result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/deleteDataByBatch")
    public Result deleteDataByBatch(@PathParam("tableName") String tableName,
            @PathParam("columnNames") String columnNames, @PathParam("columnValues") String columnValues)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.DELETE_SQL, tableName, columnNames, columnValues);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * create table
     *
     * @param tableName table name
     * @param sequenceName sequence name
     * @return create table result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/createTableByBatch")
    public Result createTableByBatch(@PathParam("tableName") String tableName,
            @PathParam("sequenceName") String sequenceName)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.CREATE_TABLE_SQL, tableName, sequenceName);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * delete table
     *
     * @param tableName table name
     * @return delete table result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/deleteTableByBatch")
    public Result deleteTableByBatch(@PathParam("tableName") String tableName)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.DELETE_TABLE_SQL, tableName);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * create index
     *
     * @param indexName index name
     * @param tableName table name
     * @param columnName column name
     * @return create index result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/createIndexByBatch")
    public Result createIndexByBatch(@PathParam("tableName") String tableName,
            @PathParam("indexName") String indexName, @PathParam("columnName") String columnName)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.CREATE_INDEX_SQL, indexName, tableName, columnName);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * delete index
     *
     * @param indexName index name
     * @return delete index result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/deleteIndexByBatch")
    public Result deleteIndexByBatch(@PathParam("indexName") String indexName)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.DELETE_INDEX_SQL, indexName);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * create sequence
     *
     * @param sequenceName sequence name
     * @return create sequence result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/createSequenceByBatch")
    public Result createSequenceByBatch(@PathParam("sequenceName") String sequenceName) throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.CREATE_SEQUENCE_SQL, sequenceName);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * delete sequence
     *
     * @param sequenceName sequence name
     * @return create sequence result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/deleteSequenceByBatch")
    public Result deleteSequenceByBatch(@PathParam("sequenceName") String sequenceName) throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.DELETE_SEQUENCE_SQL, sequenceName);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    /**
     * alter table
     *
     * @param tableName table name
     * @param columnName column name
     * @param columnType column type
     * @return alter table result
     * @throws SQLException create connection or execute sql exception
     */
    @GetMapping("/batch/alterTable")
    public Result alterTableByBatch(@PathParam("tableName") String tableName,
            @PathParam("columnName") String columnName, @PathParam("columnType") String columnType)
            throws SQLException {
        String sql = String.format(Locale.ROOT, DatabaseConstant.ALTER_TABLE_SQL, tableName, columnName, columnType);
        try (Statement statement = connection.createStatement()) {
            statement.addBatch(sql);
            int[] rowsAffected = statement.executeBatch();
            return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, rowsAffected);
        }
    }

    @Override
    public void afterPropertiesSet() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
    }

    /**
     * Used to check if the process starts properly
     *
     * @return string
     */
    @GetMapping("/checkStatus")
    public Result checkStatus() {
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, null, null);
    }
}
