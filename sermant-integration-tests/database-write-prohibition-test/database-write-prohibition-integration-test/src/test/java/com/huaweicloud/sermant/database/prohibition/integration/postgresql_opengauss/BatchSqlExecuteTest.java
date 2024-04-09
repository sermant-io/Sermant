/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.database.prohibition.integration.postgresql_opengauss;

import com.huaweicloud.sermant.database.prohibition.integration.constant.DatabaseConstant;
import com.huaweicloud.sermant.database.prohibition.integration.entity.Result;
import com.huaweicloud.sermant.database.prohibition.integration.AbstractDatabaseProhibitionTest;
import com.huaweicloud.sermant.database.prohibition.integration.utils.DynamicConfigUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Database write prohibition test
 *
 * @author zhp
 * @since 2024-03-13
 */
public class BatchSqlExecuteTest extends AbstractDatabaseProhibitionTest {
    private static final String CREATE_SEQUENCE_URL = "http://127.0.0.1:8081/batch/createSequenceByBatch?";

    private static final String DELETE_SEQUENCE_URL = "http://127.0.0.1:8081/batch/deleteSequenceByBatch?";

    private static final String CREATE_TABLE_URL = "http://127.0.0.1:8081/batch/createTableByBatch?";

    private static final String DELETE_TABLE_URL = "http://127.0.0.1:8081/batch/deleteTableByBatch?";

    private static final String INSERT_URL = "http://127.0.0.1:8081/batch/saveDataByBatch?";

    private static final String SELECT_URL = "http://127.0.0.1:8081/batch/getDataByBatch?";

    private static final String UPDATE_URL = "http://127.0.0.1:8081/batch/updateDataByBatch?";

    private static final String DELETE_URL = "http://127.0.0.1:8081/batch/deleteDataByBatch?";

    private static final String CREATE_INDEX_URL = "http://127.0.0.1:8081/batch/createIndexByBatch?";

    private static final String DELETE_INDEX_URL = "http://127.0.0.1:8081/batch/deleteIndexByBatch?";

    private static final String ALTER_TABLE_URL = "http://127.0.0.1:8081/batch/alterTable?";

    /**
     * test postgresql and opengauss database write prohibition function
     */
    @Test
    @EnabledIfSystemProperty(named = "database.prohibition.integration.test.type", matches = "POSTGRESQL_OPENGAUSS")
    public void testPostgresqlAndOpenGauss() throws Exception {
        DynamicConfigUtils.updateConfig(DISABLE_DATABASE_WRITE_PROHIBITION_CONFIG);
        Thread.sleep(2000);
        // test create sequence
        testHttpRequest(CREATE_SEQUENCE_URL + "sequenceName=students_seq", DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test create table
        testHttpRequest(CREATE_TABLE_URL + "sequenceName=students_seq&tableName=students",
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test insert data
        Result result = testHttpRequest(INSERT_URL + "tableName=students&columnNames=name,"
                + "age&columnValues='lili',11", DatabaseConstant.OPERATION_SUCCEED_CODE);
        int id = (int) result.getData();
        // test select data
        testHttpRequest(SELECT_URL + "tableName=students&columnName=id&columnValue=100",
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test update data
        testHttpRequest(UPDATE_URL + "tableName=students&columnNames=name,id&columnValues="
                + "'lili'," + id, DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test create index
        testHttpRequest(CREATE_INDEX_URL + "tableName=students&columnName=age&indexName=idx_age",
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test alter table
        testHttpRequest(ALTER_TABLE_URL + "tableName=students&columnName=class&columnType=varchar(255)",
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // enable database write prohibition
        DynamicConfigUtils.updateConfig(ENABLE_DATABASE_WRITE_PROHIBITION_CONFIG);
        Thread.sleep(2000);
        // test create sequence
        testHttpRequest(CREATE_SEQUENCE_URL + "sequenceName=students_new_seq",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test create table
        testHttpRequest(CREATE_TABLE_URL + "sequenceName=students_new_seq&tableName=students_new",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test insert data
        testHttpRequest(INSERT_URL + "columnNames=name,age&columnValues='line',11",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test select data
        testHttpRequest(SELECT_URL + "tableName=students&columnName=id&columnValue=" + id,
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test update data
        testHttpRequest(UPDATE_URL + "columnNames=name,id&columnValues='lili'," + id,
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test create index
        testHttpRequest(CREATE_INDEX_URL + "tableName=students&columnName=age&indexName=idx_age",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test delete data
        testHttpRequest(DELETE_URL + "tableName=students&columnNames=id&columnValues=" + id,
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test drop index
        testHttpRequest(DELETE_INDEX_URL + "indexName=idx_age",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test drop table
        testHttpRequest(CREATE_TABLE_URL + "tableName=students",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test drop sequence
        testHttpRequest(DELETE_SEQUENCE_URL + "sequenceName=students_seq",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // test alter table
        testHttpRequest(ALTER_TABLE_URL + "tableName=students&columnName=grade&columnType=varchar(255)",
                DatabaseConstant.OPERATION_FAIL_CODE);
        // disable database write prohibition
        DynamicConfigUtils.updateConfig(DISABLE_DATABASE_WRITE_PROHIBITION_CONFIG);
        Thread.sleep(2000);
        // test delete data
        testHttpRequest(DELETE_URL + "tableName=students&columnNames=id&columnValues=" + id,
                DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test drop index
        testHttpRequest(DELETE_INDEX_URL + "indexName=idx_age", DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test drop table
        testHttpRequest(DELETE_TABLE_URL + "tableName=students", DatabaseConstant.OPERATION_SUCCEED_CODE);
        // test drop sequence
        testHttpRequest(DELETE_SEQUENCE_URL + "sequenceName=students_seq", DatabaseConstant.OPERATION_SUCCEED_CODE);
    }
}
