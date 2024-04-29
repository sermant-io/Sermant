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

package io.sermant.database.prohibition.integration.postgresql_opengauss;

import io.sermant.database.prohibition.integration.constant.DatabaseConstant;
import io.sermant.database.prohibition.integration.entity.Result;
import io.sermant.database.prohibition.integration.AbstractDatabaseProhibitionTest;
import io.sermant.database.prohibition.integration.utils.DynamicConfigUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Database write prohibition test
 *
 * @author zhp
 * @since 2024-03-13
 */
public class StaticSqlExecuteTest extends AbstractDatabaseProhibitionTest {
    private static final String CREATE_SEQUENCE_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/createSequenceByStaticSql?";

    private static final String DELETE_SEQUENCE_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/deleteSequenceByStaticSql?";

    private static final String CREATE_TABLE_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/createTableByStaticSql?";

    private static final String DELETE_TABLE_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/deleteTableByStaticSql?";

    private static final String INSERT_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/saveDataByStaticSql?";

    private static final String SELECT_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/getDataByStaticSql?";

    private static final String UPDATE_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/updateDataByStaticSql?";

    private static final String DELETE_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/deleteDataByStaticSql?";

    private static final String CREATE_INDEX_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/createIndexByStaticSql?";

    private static final String DELETE_INDEX_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/deleteIndexByStaticSql?";

    private static final String ALTER_TABLE_BY_STATIC_SQL_URL = "http://127.0.0.1:8081/static/alterTable?";

    /**
     * test postgresql and opengauss database write prohibition function
     */
    @Test
    @EnabledIfSystemProperty(named = "database.prohibition.integration.test.type", matches = "POSTGRESQL_OPENGAUSS")
    public void testPostgresqlAndOpenGauss() throws Exception {
        DynamicConfigUtils.updateConfig(DISABLE_DATABASE_WRITE_PROHIBITION_CONFIG);
        Thread.sleep(2000);
        // test create sequence
        testHttpRequest(CREATE_SEQUENCE_BY_STATIC_SQL_URL + "sequenceName=students_seq", DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test create table
        testHttpRequest(CREATE_TABLE_BY_STATIC_SQL_URL + "sequenceName=students_seq&tableName=students",
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test insert data
        Result result = testHttpRequest(INSERT_BY_STATIC_SQL_URL + "tableName=students&columnNames=name,"
                + "age&columnValues='lili',11", DatabaseConstant.OPERATION_SUCCEED_CODE);
        int id = (int) result.getData();
        // test select data
        testHttpRequest(SELECT_BY_STATIC_SQL_URL + "tableName=students&columnName=id&columnValue=" + id,
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test update data
        testHttpRequest(UPDATE_BY_STATIC_SQL_URL + "tableName=students&columnNames=name,id&columnValues="
                + "'lili'," + id, DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test create index
        testHttpRequest(CREATE_INDEX_BY_STATIC_SQL_URL + "tableName=students&columnName=age&indexName=idx_age",
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test alter table
        testHttpRequest(ALTER_TABLE_BY_STATIC_SQL_URL + "tableName=students&columnName=class&columnType=varchar(255)",
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // enable database write prohibition
        DynamicConfigUtils.updateConfig(ENABLE_DATABASE_WRITE_PROHIBITION_CONFIG);
        Thread.sleep(2000);
        // test create sequence
        testHttpRequest(CREATE_SEQUENCE_BY_STATIC_SQL_URL + "sequenceName=students_new_seq",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test create table
        testHttpRequest(CREATE_TABLE_BY_STATIC_SQL_URL + "sequenceName=students_new_seq&tableName=students_new",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test insert data
        testHttpRequest(INSERT_BY_STATIC_SQL_URL + "columnNames=name,age&columnValues='line',11",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test select data
        testHttpRequest(SELECT_BY_STATIC_SQL_URL + "tableName=students&columnName=id&columnValue=" + id,
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test update data
        testHttpRequest(UPDATE_BY_STATIC_SQL_URL + "columnNames=name,id&columnValues='lili'," + id,
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test create index
        testHttpRequest(CREATE_INDEX_BY_STATIC_SQL_URL + "tableName=students&columnName=age&indexName=idx_age",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test delete data
        testHttpRequest(DELETE_BY_STATIC_SQL_URL + "tableName=students&columnNames=id&columnValues=" + id,
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test drop index
        testHttpRequest(DELETE_INDEX_BY_STATIC_SQL_URL + "indexName=idx_age",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test drop table
        testHttpRequest(CREATE_TABLE_BY_STATIC_SQL_URL + "tableName=students",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test drop sequence
        testHttpRequest(DELETE_SEQUENCE_BY_STATIC_SQL_URL + "sequenceName=students_seq",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test alter table
        testHttpRequest(ALTER_TABLE_BY_STATIC_SQL_URL + "tableName=students&columnName=grade&columnType=varchar(255)",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // disable database write prohibition
        DynamicConfigUtils.updateConfig(DISABLE_DATABASE_WRITE_PROHIBITION_CONFIG);
        Thread.sleep(2000);
        // test delete data
        testHttpRequest(DELETE_BY_STATIC_SQL_URL + "tableName=students&columnNames=id&columnValues=" + id,
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test drop index
        testHttpRequest(DELETE_INDEX_BY_STATIC_SQL_URL + "indexName=idx_age", DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test drop table
        testHttpRequest(DELETE_TABLE_BY_STATIC_SQL_URL + "tableName=students", DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test drop sequence
        testHttpRequest(DELETE_SEQUENCE_BY_STATIC_SQL_URL + "sequenceName=students_seq", DatabaseConstant.OPERATION_SUCCEED_CODE);
    }
}
