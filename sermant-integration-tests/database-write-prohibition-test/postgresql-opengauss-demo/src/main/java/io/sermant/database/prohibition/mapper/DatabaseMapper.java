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

package io.sermant.database.prohibition.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * Database Mapper
 *
 * @author zhp
 * @since 2024-01-13
 */
@Mapper
public interface DatabaseMapper {
    /**
     * create table
     *
     * @param tableName table name
     * @param sequenceName sequence name
     */
    @Update("CREATE TABLE ${tableName} (id int4 NOT NULL DEFAULT nextval('${sequenceName}'::regclass),name varchar"
            + "(255),age int4)")
    void createTable(String tableName, String sequenceName);

    /**
     * delete table
     *
     * @param tableName table name
     */
    @Update("DROP TABLE ${tableName}")
    void deleteTable(String tableName);

    /**
     * create sequence
     *
     * @param sequenceName sequence Name
     */
    @Update("CREATE SEQUENCE ${sequenceName} INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1")
    void createSequence(String sequenceName);

    /**
     * delete sequence
     *
     * @param sequenceName sequence Name
     */
    @Update("DROP SEQUENCE ${sequenceName}")
    void deleteSequence(String sequenceName);

    /**
     * create index
     *
     * @param indexName index name
     * @param tableName table name
     * @param columnName column name
     */
    @Update("CREATE INDEX ${indexName} ON ${tableName} (${columnName})")
    void createIndex(String indexName, String tableName, String columnName);

    /**
     * delete index
     *
     * @param indexName index name
     */
    @Update("DROP INDEX ${indexName}")
    void deleteIndex(String indexName);

    /**
     * alter table
     *
     * @param tableName table name
     * @param columnName column name
     * @param columnType column type
     */
    @Update("ALTER TABLE ${tableName} ADD COLUMN ${columnName} ${columnType}")
    void addColumn(String tableName, String columnName, String columnType);
}
