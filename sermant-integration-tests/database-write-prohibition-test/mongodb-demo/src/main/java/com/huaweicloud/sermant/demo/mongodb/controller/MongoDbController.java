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

package com.huaweicloud.sermant.demo.mongodb.controller;

import com.huaweicloud.sermant.database.prohibition.common.constant.DatabaseConstant;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Arrays;

import javax.annotation.PostConstruct;

/**
 * operate mongodb
 *
 * @author daizhenyu
 * @since 2024-03-11
 **/
@RestController
public class MongoDbController {
    @Value("${mongodb.address}")
    private String mongoDbAddress;

    private MongoClient mongoClient;

    /**
     * mongoClient init
     */
    @PostConstruct
    public void init() {
        mongoClient = MongoClients.create(mongoDbAddress);
    }

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
     * createCollection
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @return int prohibition status code
     */
    @RequestMapping("createCollection")
    public String createCollection(String databaseName, String collectionName) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            createCollection(database, collectionName);
        } catch (SQLException e) {
            if (e.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    private void createCollection(MongoDatabase database, String collectionName) throws SQLException {
        database.createCollection(collectionName);
    }

    /**
     * dropCollection
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @return int prohibition status code
     */
    @RequestMapping("dropCollection")
    public String dropCollection(String databaseName, String collectionName) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            dropCollection(database, collectionName);
        } catch (SQLException e) {
            if (e.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    private void dropCollection(MongoDatabase database, String collectionName) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.drop();
    }

    /**
     * createIndex
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @param fieldName field name
     * @return int prohibition status code
     */
    @RequestMapping("createIndex")
    public String createIndex(String databaseName, String collectionName, String fieldName) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            createIndex(database, collectionName, fieldName);
        } catch (SQLException e) {
            if (e.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    private void createIndex(MongoDatabase database, String collectionName, String fieldName) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        IndexOptions indexOptions = new IndexOptions().unique(true);
        collection.createIndex(Indexes.ascending(fieldName), indexOptions);
    }

    /**
     * deleteIndex
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @param fieldName field name
     * @return int prohibition status code
     */
    @RequestMapping("deleteIndex")
    public String deleteIndex(String databaseName, String collectionName, String fieldName) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            deleteIndex(database, collectionName, fieldName);
        } catch (SQLException e) {
            if (e.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    private void deleteIndex(MongoDatabase database, String collectionName, String fieldName) throws SQLException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.dropIndex(Indexes.ascending(fieldName));
    }

    /**
     * aggregateDocument
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @param fieldName field name
     * @param value field value
     * @return int number of documents
     */
    @RequestMapping("aggregate")
    public int aggregate(String databaseName, String collectionName, String fieldName, String value) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        AggregateIterable<Document> aggregate = collection.aggregate(
                Arrays.asList(Aggregates.match(Filters.eq(fieldName, value)))
        );
        int count = 0;
        for (Document document : aggregate) {
            count++;
        }
        return count;
    }

    /**
     * findDocument
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @return int number of documents
     */
    @RequestMapping("find")
    public int findDocument(String databaseName, String collectionName) {
        int documentCount = 0;
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> documents = collection.find();
        for (Document document : documents) {
            documentCount++;
        }
        return documentCount;
    }

    /**
     * insertDocument
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @param fieldName field name
     * @param value field value
     * @return int prohibition status code
     */
    @RequestMapping("insert")
    public String insertDocument(String databaseName, String collectionName, String fieldName, String value) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            insertDocument(database, collectionName, fieldName, value);
        } catch (SQLException e) {
            if (e.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    private void insertDocument(MongoDatabase database, String collectionName, String fieldName, String value)
            throws SQLException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document document = new Document(fieldName, value);
        collection.insertOne(document);
    }

    /**
     * deleteDocument
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @param fieldName field name
     * @param value field value
     * @return int prohibition status code
     */
    @RequestMapping("delete")
    public String deleteDocument(String databaseName, String collectionName, String fieldName, String value) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            deleteDocument(database, collectionName, fieldName, value);
        } catch (SQLException e) {
            if (e.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    private void deleteDocument(MongoDatabase database, String collectionName, String fieldName, String value)
            throws SQLException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.deleteOne(
                Filters.eq(fieldName, value)
        );
    }

    /**
     * deleteDocument
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @param fieldName field name
     * @param value field value
     * @return int prohibition status code
     */
    @RequestMapping("update")
    public String updateDocument(String databaseName, String collectionName, String fieldName, String value) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            updateDocument(database, collectionName, fieldName, value);
        } catch (SQLException e) {
            if (e.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    private void updateDocument(MongoDatabase database, String collectionName, String fieldName, String value)
            throws SQLException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.updateOne(
                Filters.eq(fieldName, value),
                new Document("$set", new Document(fieldName, "newValue")),
                new UpdateOptions().upsert(true)
        );
    }

    /**
     * replaceDocument
     *
     * @param databaseName database name
     * @param collectionName collection name
     * @param fieldName field name
     * @param value field value
     * @return int prohibition status code
     */
    @RequestMapping("replace")
    public String replaceDocument(String databaseName, String collectionName, String fieldName, String value) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            replaceDocument(database, collectionName, fieldName, value);
        } catch (SQLException e) {
            if (e.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
                return DatabaseConstant.OPERATION_FAIL_CODE;
            }
        }
        return DatabaseConstant.OPERATION_SUCCEED_CODE;
    }

    private void replaceDocument(MongoDatabase database, String collectionName, String fieldName, String value)
            throws SQLException {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.replaceOne(Filters.eq(fieldName, value),
                new Document(fieldName, "newValue"));
    }
}
