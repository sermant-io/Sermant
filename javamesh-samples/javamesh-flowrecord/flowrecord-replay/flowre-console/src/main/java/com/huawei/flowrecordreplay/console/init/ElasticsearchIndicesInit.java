/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.init;

import com.huawei.flowrecordreplay.console.util.Constant;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 初始化创建必需的elasticsearch的索引
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-05-30
 */
@Component
public class ElasticsearchIndicesInit implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchIndicesInit.class);

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void run(ApplicationArguments args) {
        if (!checkIndexExistence(Constant.RECORD_JOB_INDEX)) {
            createRecordJobIndex();
        }

        if (!checkIndexExistence(Constant.REPLAY_JOB_INDEX)) {
            createReplayJobIndex();
        }

        if (!checkIndexExistence(Constant.IGNORE_FIELDS_INDEX)) {
            createIgnoreFieldsIndex();
        }
    }

    /**
     * 创建录制任务索引
     */
    private void createReplayJobIndex() {
        try {
            CreateIndexRequest request = new CreateIndexRequest(Constant.REPLAY_JOB_INDEX);
            request.settings(Settings.builder().put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1));
            XContentBuilder builder = JsonXContent.contentBuilder()
                .startObject()
                .startObject("properties")
                .startObject("name").field("type", "keyword").endObject()
                .startObject("jobId").field("type", "keyword").endObject()
                .startObject("recordJobId").field("type", "keyword").endObject()
                .startObject("recordJobName").field("type", "keyword").endObject()
                .startObject("application").field("type", "keyword").endObject()
                .startObject("recordIndexList").field("type", "text").startObject("fields")
                .startObject("keyword").field("type", "keyword").endObject().endObject().endObject()
                .startObject("address").field("type", "text").startObject("fields")
                .startObject("keyword").field("type", "keyword").endObject().endObject().endObject()
                .startObject("from").field("type", "date").endObject()
                .startObject("to").field("type", "date").endObject()
                .startObject("timeStamp").field("type", "long").endObject()
                .startObject("status").field("type", "keyword").endObject()
                .startObject("modifyRule").field("type", "nested").endObject()
                .startObject("mockMethods").field("type", "nested").endObject()
                .startObject("stressTestType").field("type", "keyword").endObject()
                .startObject("baselineThroughPut").field("type", "keyword").endObject()
                .startObject("maxThreadCount").field("type", "keyword").endObject()
                .startObject("maxResponseTime").field("type", "keyword").endObject()
                .startObject("minSuccessRate").field("type", "keyword").endObject()
                .endObject()
                .endObject();
            request.mapping(builder);
            restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.error("Create replay job index error, {}", e.getMessage());
        }
    }

    /**
     * 创建录制任务索引
     */
    private void createRecordJobIndex() {
        try {
            CreateIndexRequest request = new CreateIndexRequest(Constant.RECORD_JOB_INDEX);
            request.settings(Settings.builder().put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1));
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties")
                .startObject("name").field("type", "keyword").endObject()
                .startObject("jobId").field("type", "keyword").endObject()
                .startObject("application").field("type", "keyword").endObject()
                .startObject("machineList").field("type", "text").startObject("fields")
                .startObject("keyword").field("type", "keyword").endObject().endObject().endObject()
                .startObject("methodList").field("type", "text").startObject("fields")
                .startObject("keyword").field("type", "keyword").endObject().endObject().endObject()
                .startObject("trigger").field("type", "keyword").endObject()
                .startObject("extra").field("type", "text").startObject("fields")
                .startObject("keyword").field("type", "keyword").endObject().endObject().endObject()
                .startObject("status").field("type", "keyword").endObject()
                .startObject("startTime").field("type", "date").endObject()
                .startObject("endTime").field("type", "date").endObject()
                .startObject("timeStamp").field("type", "long").endObject()
                .endObject()
                .endObject();
            request.mapping(builder);
            restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.error("Create record job index error, {}", e.getMessage());
        }
    }

    /**
     * 创建忽略字段索引
     */
    private void createIgnoreFieldsIndex() {
        try {
            CreateIndexRequest request = new CreateIndexRequest(Constant.IGNORE_FIELDS_INDEX);
            request.settings(Settings.builder().put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1));
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties")
                .startObject("method").field("type", "keyword").endObject()
                .startObject("fields")
                .startObject("properties")
                .startObject("name").field("type", "keyword").endObject()
                .startObject("ignore").field("type", "boolean").endObject()
                .endObject()
                .endObject()
                .endObject()
                .endObject();
            request.mapping(builder);
            restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.error("Create ignore fields index error, {}", e.getMessage());
        }
    }

    /**
     * 检查索引是否存在
     */
    private boolean checkIndexExistence(String index) {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        try {
            return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException ioException) {
            LOGGER.error("Check index exist error , {}", ioException.getMessage());
        }
        return false;
    }
}