/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowre.mockserver.datasource;

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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
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

    private static final String RETURN_BLANK = "";

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 添加数据
     *
     * @param index 待添加数据的表名
     * @param data  待添加的数据
     */
    public void addData(String index, Object data) throws IOException {
        IndexRequest request = new IndexRequest(index);
        request.source(JSON.toJSONString(data), XContentType.JSON);
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 按doc粒度更新数据库
     *
     * @param index 待更新的表索引
     * @param docId 待更新的doc 的id
     * @param data  新的数据
     */
    public void update(String index, String docId, Object data) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(index, docId);
        updateRequest.doc(JSON.toJSONString(data), XContentType.JSON);
        updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
    }

    /**
     * 滚动查询索引的全部doc
     *
     * @param index 表名的索引
     * @return List SearchHit 返回查询结果的列表
     */
    public List<String> getAllData(String index) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.scroll(TimeValue.timeValueMinutes(EsClient.SCROLL_TIME));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(EsClient.SCROLL_SIZE);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        return search(searchRequest);
    }

    /**
     * 通过字段搜索数据
     *
     * @param index        数据表index
     * @param keyWord      查找的字段
     * @param keyWordValue 查找字段的值
     * @return 返回搜索到数据的字符串列表
     */
    public List<String> searchByKey(String index, String keyWord, String keyWordValue) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.scroll(TimeValue.timeValueMinutes(EsClient.SCROLL_TIME));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(EsClient.SCROLL_SIZE);
        searchSourceBuilder.query(QueryBuilders.termQuery(keyWord, keyWordValue));
        searchRequest.source(searchSourceBuilder);
        return search(searchRequest);
    }

    List<String> search(SearchRequest searchRequest) throws IOException {
        List<String> searchResult = new ArrayList<>();
        SearchResponse searchResponse = restHighLevelClient
                .search(searchRequest,
                        RequestOptions.DEFAULT);
        while (searchResponse.getHits().getHits() != null && searchResponse.getHits().getHits().length > 0) {
            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                searchResult.add(searchHit.getSourceAsString());
            }
            SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
            searchScrollRequest.scroll(TimeValue.timeValueMinutes(EsClient.SCROLL_TIME));
            searchResponse = restHighLevelClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
        }
        return searchResult;
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
            return RETURN_BLANK;
        }
    }

    /**
     * 通过关键字段查找一个数据
     *
     * @param index        数据库索引
     * @param keyWord      关键字段
     * @param keyWordValue 关键字段的值
     * @return 返回查找结果中的一个
     * @throws IOException 抛出IOException
     */
    public String getOne(String index, String keyWord, String keyWordValue) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(keyWord, keyWordValue));
        searchSourceBuilder.size(1);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse == null) {
            return RETURN_BLANK;
        }
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        if (searchHits.length > 0) {
            return searchHits[0].getSourceAsString();
        } else {
            return RETURN_BLANK;
        }
    }

    public boolean checkIndexExistence(String index) {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        try {
            return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException ioException) {
            LOGGER.error("Check index exist error , {}", ioException.getMessage());
        }
        return false;
    }
}
