/*
 *  Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.database.prohibition.integration.mongodb;

import com.huaweicloud.sermant.database.prohibition.integration.constant.DatabaseConstant;
import com.huaweicloud.sermant.database.prohibition.integration.utils.DynamicConfigUtils;
import com.huaweicloud.sermant.database.prohibition.integration.utils.HttpRequestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * mongodb write prohibition integration test
 *
 * @author daizhenyu
 * @since 2024-03-12
 **/
@EnabledIfSystemProperty(named = "database.write.prohibition.integration.test.type", matches = "MONGODB")
public class MongoDbProhibitionTest {
    private static final String MONGODB_CONFIG_ON =
            "enableMongoDbWriteProhibition: true" + DatabaseConstant.LINE_SEPARATOR +
                    "mongoDbDatabases:" + DatabaseConstant.LINE_SEPARATOR +
                    " - test";

    private static final String MONGODB_CONFIG_OFF =
            "enableMongoDbWriteProhibition: false" + DatabaseConstant.LINE_SEPARATOR +
                    "mongoDbDatabases:" + DatabaseConstant.LINE_SEPARATOR +
                    " - test";

    @BeforeAll
    public static void before() throws Exception {
        DynamicConfigUtils.updateConfig(MONGODB_CONFIG_OFF);
        Thread.sleep(3000);

        // prepare test data
        HttpRequestUtils.doGet("http://127.0.0.1:9098/createCollection?databaseName=test&collectionName=find");
        HttpRequestUtils.doGet("http://127.0.0.1:9098/insert?databaseName=test&collectionName=find&fieldName"
                + "=key1&value=value1");
        HttpRequestUtils
                .doGet("http://127.0.0.1:9098/createCollection?databaseName=test&collectionName=drop");
        HttpRequestUtils
                .doGet("http://127.0.0.1:9098/createCollection?databaseName=test&collectionName=data");
        HttpRequestUtils
                .doGet("http://127.0.0.1:9098/insert?databaseName=test&collectionName=data");
        HttpRequestUtils.doGet("http://127.0.0.1:9098/insert?databaseName=test&collectionName=data&fieldName"
                + "=key1&value=value1");
        HttpRequestUtils.doGet("http://127.0.0.1:9098/insert?databaseName=test&collectionName=data&fieldName"
                + "=key1&value=value2");
        HttpRequestUtils.doGet("http://127.0.0.1:9098/insert?databaseName=test&collectionName=data&fieldName"
                + "=key1&value=value3");
        HttpRequestUtils.doGet("http://127.0.0.1:9098/insert?databaseName=test&collectionName=data&fieldName"
                + "=key2&value=value4");
        HttpRequestUtils.doGet("http://127.0.0.1:9098/createIndex?databaseName=test&collectionName=data&fieldName"
                + "=key1");

        DynamicConfigUtils.updateConfig(MONGODB_CONFIG_ON);
        Thread.sleep(3000);
    }

    /**
     * find scene
     */
    @Test
    public void testFind() {
        Assertions.assertEquals(DatabaseConstant.DATA_COUNT, HttpRequestUtils
                .doGet("http://127.0.0.1:9098/find?databaseName=test&collectionName=find"));
        Assertions.assertEquals(DatabaseConstant.DATA_COUNT, HttpRequestUtils
                .doGet("http://127.0.0.1:9098/aggregate?databaseName=test&collectionName=find&fieldName"
                        + "=key1&value=value1"));
    }

    /**
     * create collection
     */
    @Test
    public void testCreateCollection() {
        Assertions.assertEquals(DatabaseConstant.OPERATION_FAIL_CODE, HttpRequestUtils
                .doGet("http://127.0.0.1:9098/createCollection?databaseName=test&collectionName=create"));
    }

    /**
     * drop collection
     */
    @Test
    public void testDropCollection() {
        Assertions.assertEquals(DatabaseConstant.OPERATION_FAIL_CODE, HttpRequestUtils
                .doGet("http://127.0.0.1:9098/dropCollection?databaseName=test&collectionName=drop"));
    }

    /**
     * insert document
     */
    @Test
    public void testInsert() {
        Assertions.assertEquals(DatabaseConstant.OPERATION_FAIL_CODE,
                HttpRequestUtils.doGet("http://127.0.0.1:9098/insert?"
                        + "databaseName=test&collectionName=data&fieldName=key1&value=value5"));
    }

    /**
     * update document
     */
    @Test
    public void testUpdate() {
        Assertions.assertEquals(DatabaseConstant.OPERATION_FAIL_CODE,
                HttpRequestUtils.doGet("http://127.0.0.1:9098/update?"
                        + "databaseName=test&collectionName=data&fieldName=key1&value=value1"));
    }

    /**
     * replace document
     */
    @Test
    public void testReplace() {
        Assertions.assertEquals(DatabaseConstant.OPERATION_FAIL_CODE,
                HttpRequestUtils.doGet("http://127.0.0.1:9098/replace?"
                        + "databaseName=test&collectionName=data&fieldName=key1&value=value2"));
    }

    /**
     * delete document
     */
    @Test
    public void testDelete() {
        Assertions.assertEquals(DatabaseConstant.OPERATION_FAIL_CODE,
                HttpRequestUtils.doGet("http://127.0.0.1:9098/replace?"
                        + "databaseName=test&collectionName=data&fieldName=key1&value=value3"));
    }

    /**
     * create index
     */
    @Test
    public void testCreateIndex() {
        Assertions.assertEquals(DatabaseConstant.OPERATION_FAIL_CODE,
                HttpRequestUtils.doGet("http://127.0.0.1:9098/createIndex?"
                        + "databaseName=test&collectionName=data&fieldName=key2"));
    }

    /**
     * delete index
     */
    @Test
    public void testDeleteIndex() {
        Assertions.assertEquals(DatabaseConstant.OPERATION_FAIL_CODE,
                HttpRequestUtils.doGet("http://127.0.0.1:9098/deleteIndex?"
                        + "databaseName=test&collectionName=data&fieldName=key1"));
    }
}
