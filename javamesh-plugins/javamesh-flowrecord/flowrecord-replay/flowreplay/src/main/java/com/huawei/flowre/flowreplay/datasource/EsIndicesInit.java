/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.datasource;

import com.huawei.flowre.flowreplay.config.Const;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用于初始化 ES 表格
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-12
 */
@Component
public class EsIndicesInit {
    private static final String INDEX_NUMBER_OF_SHARDS = "index.number_of_shards";

    private static final String INDEX_NUMBER_OF_REPLICAS = "index.number_of_replicas";

    private static final String FIELD_TYPE = "type";

    private static final String KEYWORD = "keyword";

    private static final String PROPERTIES = "properties";

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 初始化回放结果存放index
     *
     * @param index
     * @throws IOException
     */
    public void replayResult(String index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder().put(INDEX_NUMBER_OF_SHARDS, 1)
            .put(INDEX_NUMBER_OF_REPLICAS, 1));
        XContentBuilder builder = XContentFactory.jsonBuilder()
            .startObject()
            .startObject(PROPERTIES)
            .startObject("traceId").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("method").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("recordTime").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("replayTime").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("responseTime").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("statusCode").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("compareResult").field(FIELD_TYPE, "boolean").endObject()

            .startObject("fieldCompare")
            .startObject(PROPERTIES)
            .startObject("name").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("record").field(FIELD_TYPE, "text").endObject()
            .startObject("replay").field(FIELD_TYPE, "text").endObject()
            .startObject("compare").field(FIELD_TYPE, "boolean").endObject()
            .startObject("ignore").field(FIELD_TYPE, "boolean").endObject()
            .endObject()
            .endObject()
            .endObject()
            .endObject();
        request.mapping(builder);
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 新建接口忽略字段的表
     *
     * @throws IOException
     */
    public void fieldsIgnore() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(Const.IGNORE_FIELDS_INDEX);
        request.settings(Settings.builder().put(INDEX_NUMBER_OF_SHARDS, 1)
            .put(INDEX_NUMBER_OF_REPLICAS, 1));
        XContentBuilder builder = XContentFactory.jsonBuilder()
            .startObject()
            .startObject(PROPERTIES)
            .startObject("method").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("fields")
            .startObject(PROPERTIES)
            .startObject("name").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("ignore").field(FIELD_TYPE, "boolean").endObject()
            .endObject()
            .endObject()
            .endObject()
            .endObject();
        request.mapping(builder);
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 新建replay_metric 的表
     *
     * @throws IOException
     */
    public void replayMetric() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(Const.REPLAY_METRIC_INDEX);
        request.settings(Settings.builder().put(INDEX_NUMBER_OF_SHARDS, 1)
            .put(INDEX_NUMBER_OF_REPLICAS, 1));
        XContentBuilder builder = XContentFactory.jsonBuilder()
            .startObject()
            .startObject(PROPERTIES)
            .startObject("replayWorkerName").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("replayJobId").field(FIELD_TYPE, KEYWORD).endObject()
            .startObject("timeStamp").field(FIELD_TYPE, "date").endObject()
            .startObject("rps").field(FIELD_TYPE, "integer").endObject()
            .startObject("threadCount").field(FIELD_TYPE, "integer").endObject()
            .endObject()
            .endObject();
        request.mapping(builder);
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }
}
