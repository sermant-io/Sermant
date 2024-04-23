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

package io.sermant.database.prohibition.common.constant;

/**
 * common constant
 *
 * @author daizhenyu
 * @since 2024-03-11
 **/
public class DatabaseConstant {
    /**
     * database write prohibition sqlexception message prefix
     */
    public static final String SQL_EXCEPTION_MESSAGE_PREFIX = "Database prohibit to write";

    /**
     * Status code for execute database operation failure
     */
    public static final String OPERATION_FAIL_CODE = "100";

    /**
     * Status code for successful execute database operation
     */
    public static final String OPERATION_SUCCEED_CODE = "101";

    /**
     * select sql
     */
    public static final String SELECT_SQL = "SELECT NAME,AGE FROM %s WHERE %s = %s";

    /**
     * delete sql
     */
    public static final String DELETE_SQL = "DELETE FROM %s WHERE %s = %s";

    /**
     * insert sql
     */
    public static final String INSERT_SQL = "INSERT INTO %s (%s) values (%s) RETURNING id";

    /**
     * insert sql no return value
     */
    public static final String INSERT_SQL_NO_RETURN = "INSERT INTO %s (%s) values (%s)";

    /**
     * update sql
     */
    public static final String UPDATE_SQL = "UPDATE %s SET %s = %s WHERE %s = %s";

    /**
     * create table sql
     */
    public static final String CREATE_TABLE_SQL = "CREATE TABLE %s (id int4 NOT NULL DEFAULT "
            + "nextval('%s'::regclass), name varchar(255) ,age int4)";

    /**
     * delete table sql
     */
    public static final String DELETE_TABLE_SQL = "DROP TABLE %s";

    /**
     * create index sql
     */
    public static final String CREATE_INDEX_SQL = "CREATE INDEX %s ON %s (%s)";

    /**
     * delete index sql
     */
    public static final String DELETE_INDEX_SQL = "DROP INDEX %s";

    /**
     * create sequence sql
     */
    public static final String CREATE_SEQUENCE_SQL = "CREATE SEQUENCE %s INCREMENT 1 MINVALUE 1 MAXVALUE "
            + "9223372036854775807 START 1 CACHE 1";

    /**
     * delete sequence sql
     */
    public static final String DELETE_SEQUENCE_SQL = "DROP SEQUENCE %s";

    /**
     * alter table sql
     */
    public static final String ALTER_TABLE_SQL = "ALTER TABLE %s ADD COLUMN %s %s";

    /**
     * Request parameter separator
     */
    public static final String PARAM_SEPARATOR = ",";

    private DatabaseConstant() {
    }
}