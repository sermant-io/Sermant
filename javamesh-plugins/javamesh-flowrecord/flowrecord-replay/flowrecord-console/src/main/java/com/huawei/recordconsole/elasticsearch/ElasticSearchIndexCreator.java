/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.recordconsole.elasticsearch;

import com.huawei.recordconsole.config.CommonConfig;

import com.alibaba.fastjson.JSON;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ElasticSearch IndexCreator
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 *
 */

@Component
public class ElasticSearchIndexCreator {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void insert(String index, Object data) throws IOException {
        IndexRequest request = new IndexRequest(index);
        request.source(JSON.toJSONString(data), XContentType.JSON);
        restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    public void createRecordJobIndex(String index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder().put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1));
        XContentBuilder builder = JsonXContent.contentBuilder()
                .startObject()
                .startObject("properties")
                .startObject("jobId").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("traceId").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("appType").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("methodName").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("requestBody").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("requestClass").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("responseBody").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("responseClass").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .endObject().endObject();
        request.mapping(builder);
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    public void createSubcallIndex(String index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder().put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1));
        XContentBuilder builder = JsonXContent.contentBuilder()
                .startObject()
                .startObject("properties")
                .startObject("jobId").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("traceId").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("appType").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("methodName").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("subCallKey").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("requestBody").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("requestClass").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("responseBody").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .startObject("responseClass").field(CommonConfig.RECORDJOB_TYPE, CommonConfig.RECORDJOB_KEYWORD)
                .endObject()
                .endObject().endObject();
        request.mapping(builder);
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    public boolean checkIndexExistence(String index) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }
}
