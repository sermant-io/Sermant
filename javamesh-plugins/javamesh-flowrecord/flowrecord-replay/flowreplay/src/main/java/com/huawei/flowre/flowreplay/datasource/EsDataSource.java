/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.datasource;

import com.huawei.flowre.flowreplay.config.Const;

import com.alibaba.fastjson.JSON;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Elasticsearch 数据库增删改查实现
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-22
 */
@Component
public class EsDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsDataSource.class);

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 添加数据
     *
     * @param index 待添加数据的表名
     * @param data  待添加的数据
     * @return 返回添加成功的doc id
     */
    public String addData(String index, Object data) {
        IndexRequest request = new IndexRequest(index);
        request.source(JSON.toJSONString(data), XContentType.JSON);
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        try {
            return restHighLevelClient.index(request, RequestOptions.DEFAULT).getId();
        } catch (IOException ioException) {
            LOGGER.error("Insert data has error:{}", ioException.getMessage());
        }
        return Const.BLANK;
    }

    /**
     * 按doc粒度更新数据库
     *
     * @param index 待更新的表索引
     * @param docId 待更新的doc 的id
     * @param data  新的数据
     */
    public void update(String index, String docId, Object data) {
        UpdateRequest updateRequest = new UpdateRequest(index, docId);
        updateRequest.doc(JSON.toJSONString(data), XContentType.JSON);
        updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        try {
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException ioException) {
            LOGGER.error("Update data has error:{}", ioException.getMessage());
        }
    }

    /**
     * 按照字段进行更新操作
     *
     * @param index        需要更新的表索引
     * @param keyWord      需筛选字段
     * @param keyWordValue 需筛选字段的值
     * @param field        更新的字段名称
     * @param value        更新的字段值
     */
    public void updateField(String index, String keyWord, String keyWordValue, String field, String value) {
        UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(index);
        updateByQueryRequest.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termQuery(keyWord, keyWordValue)));
        Script script = new Script("ctx._source['" + field + "']='" + value + "'");
        updateByQueryRequest.setScript(script);
        updateByQueryRequest.setRefresh(true);
        try {
            restHighLevelClient.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException ioException) {
            LOGGER.error("Update field has error:{}", ioException.getMessage());
        }
    }

    /**
     * 滚动查询索引的全部doc
     *
     * @param index 表名的索引
     * @return List SearchHit 返回查询结果的列表
     */
    public List<String> getData(String index) {
        List<String> queryResult = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.scroll(TimeValue.timeValueMinutes(Const.SCROLL_TIME));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(Const.SCROLL_SIZE);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        try {
            SearchResponse searchResponse = restHighLevelClient
                .search(searchRequest.source(searchSourceBuilder),
                    RequestOptions.DEFAULT);
            while (searchResponse.getHits().getHits() != null && searchResponse.getHits().getHits().length > 0) {
                String scrollId = searchResponse.getScrollId();
                SearchHit[] searchHits = searchResponse.getHits().getHits();
                for (SearchHit searchHit : searchHits) {
                    queryResult.add(searchHit.getSourceAsString());
                }
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
                searchScrollRequest.scroll(TimeValue.timeValueMinutes(Const.SCROLL_TIME));
                searchResponse = restHighLevelClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
            }
            return queryResult;
        } catch (IOException ioException) {
            LOGGER.error("Query data has error:{}", ioException.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 通过字段搜索查找doc id
     *
     * @param index        搜索字段的表索引
     * @param keyWord      搜索的字段
     * @param keyWordValue 搜索字段的值
     * @return 返回doc id
     */
    public String getDocId(String index, String keyWord, String keyWordValue) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(keyWord, keyWordValue));
        searchSourceBuilder.size(1);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        if (searchHits.length > 0) {
            return searchHits[0].getId();
        } else {
            return Const.BLANK;
        }
    }

    /**
     * 查询索引是否存在
     *
     * @param index 索引名
     * @return 返回是否存在索引
     * @throws IOException io异常
     */
    public boolean checkIndexExistence(String index) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }
}
