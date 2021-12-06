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

package com.huawei.flowrecordreplay.console.datasource.elasticsearch;

import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordInterfaceCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayInterfaceCountEntity;
import com.huawei.flowrecordreplay.console.util.Constant;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于 ES 聚合查询
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-06
 */
@Component
public class EsDataSourceAggregate {
    /**
     * 录制methodName字段
     */
    private static final String METHOD_NAME = "methodName";

    /**
     * 回放结果字段
     */
    private static final String COMPARE_RESULT = "compareResult";

    /**
     * 回放method字段
     */
    private static final String METHOD = "method";

    @Autowired
    RestHighLevelClient esClient;

    /**
     * 统计在一次回放中所有接口的回放情况
     *
     * @param jobId 回放任务的id
     * @return 回放结果中各个接口的回放情况列表
     */
    public List<ReplayInterfaceCountEntity> replayInterfaceCount(String jobId) throws IOException {
        List<ReplayInterfaceCountEntity> replayInterfaceCountEntityList = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(Constant.REPLAY_RESULT_PREFIX + jobId);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder aggregationBuilder = AggregationBuilders.terms(COMPARE_RESULT).field(COMPARE_RESULT);
        TermsAggregationBuilder builder = AggregationBuilders
            .terms(METHOD).field(METHOD).subAggregation(aggregationBuilder);

        searchSourceBuilder.aggregation(builder);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get(METHOD);
        for (Terms.Bucket bucket : parsedStringTerms.getBuckets()) {
            ReplayInterfaceCountEntity replayInterfaceCountEntity = new ReplayInterfaceCountEntity();
            replayInterfaceCountEntity.setMethod(bucket.getKeyAsString());
            replayInterfaceCountEntity.setTotal(bucket.getDocCount());
            ParsedLongTerms parsedLongTerms = bucket.getAggregations().get(COMPARE_RESULT);
            if (!parsedLongTerms.getBuckets().isEmpty()) {
                if (String.valueOf(true).equals(parsedLongTerms.getBuckets().get(0).getKeyAsString())) {
                    replayInterfaceCountEntity
                        .setSuccessCount(parsedLongTerms.getBuckets().get(0).getDocCount());
                    replayInterfaceCountEntity
                        .setFailureCount(replayInterfaceCountEntity.getTotal()
                            - parsedLongTerms.getBuckets().get(0).getDocCount());
                } else {
                    replayInterfaceCountEntity
                        .setFailureCount(parsedLongTerms.getBuckets().get(0).getDocCount());
                    replayInterfaceCountEntity
                        .setSuccessCount(replayInterfaceCountEntity.getTotal()
                            - parsedLongTerms.getBuckets().get(0).getDocCount());
                }
            }
            replayInterfaceCountEntityList.add(replayInterfaceCountEntity);
        }
        return replayInterfaceCountEntityList;
    }

    /**
     * 统计在一次录制中所有接口的数目
     *
     * @param jobId 回放任务的id
     * @return 录制结果中各个接口的回放情况列表
     */
    public List<RecordInterfaceCountEntity> recordInterfaceCount(String jobId) throws IOException {
        List<RecordInterfaceCountEntity> recordInterfaceCountEntityList = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(jobId + Constant.STAR);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder builder = AggregationBuilders.terms(METHOD_NAME).field(METHOD_NAME);
        searchSourceBuilder.aggregation(builder);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get(METHOD_NAME);
        for (Terms.Bucket bucket : parsedStringTerms.getBuckets()) {
            RecordInterfaceCountEntity recordInterfaceCountEntity = new RecordInterfaceCountEntity();
            recordInterfaceCountEntity.setMethod(bucket.getKeyAsString());
            recordInterfaceCountEntity.setTotal(bucket.getDocCount());
            recordInterfaceCountEntityList.add(recordInterfaceCountEntity);
        }
        return recordInterfaceCountEntityList;
    }

    /**
     * 查询全部的子调用接口
     *
     * @param jobId 录制任务id
     * @return 返回子调用接口列表
     */
    public List<String> getAllSubCallMethod(String jobId) throws IOException {
        List<String> subCallMethods = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(Constant.SUB_CALL_PREFIX + jobId);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder builder = AggregationBuilders.terms(METHOD_NAME).field(METHOD_NAME);
        searchSourceBuilder.aggregation(builder);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get(METHOD_NAME);
        for (Terms.Bucket bucket : parsedStringTerms.getBuckets()) {
            subCallMethods.add(bucket.getKeyAsString());
        }
        return subCallMethods;
    }

    /**
     * 指定字段获取统计信息
     *
     * @param index   es索引
     * @param keyword 指定统计的字段
     * @return 返回统计Buckets
     * @throws IOException
     */
    public List getBuckets(String index, String keyword) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        AggregationBuilder builder = AggregationBuilders.terms(keyword).field(keyword);
        searchSourceBuilder.aggregation(builder);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get(keyword);
        return parsedStringTerms.getBuckets();
    }
}
