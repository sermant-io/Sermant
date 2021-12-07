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

import com.huawei.flowrecordreplay.console.datasource.entity.RecordJobEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.ReplayJobEntity;
import com.huawei.flowrecordreplay.console.domain.RecordJobs;
import com.huawei.flowrecordreplay.console.domain.ReplayJobs;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elasticsearch接口实现
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@Component
public class ElasticsearchJobStorageImpl implements ElasticsearchJobStorage {
    private static final Scroll SCROLL = new Scroll(TimeValue.timeValueMinutes(1L));

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 根据ID查询录制任务
     */
    @Override
    public RecordJobEntity getRecordJob(String jobId) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(Constant.JOB_ID_KEYWORD, jobId));
        searchSourceBuilder.query(boolQueryBuilder).sort(new FieldSortBuilder(Constant.TIME_STAMP_KEYWORD)
                .order(SortOrder.DESC));
        SearchResponse searchResponse = search(Constant.RECORD_JOB_INDEX, searchSourceBuilder);

        RecordJobEntity recordJobEntity = null;
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits != null && hits.length == 1) {
            String sourceAsString = hits[0].getSourceAsString();
            recordJobEntity = JSON.parseObject(sourceAsString, RecordJobEntity.class);
        }
        return recordJobEntity;
    }

    /**
     * 条件查询录制任务
     */
    @Override
    public RecordJobs getRecordJobList(String name, String application, String from, String to)
            throws IOException, ParseException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isBlank(name) && StringUtils.isBlank(application)
                && StringUtils.isBlank(from) && StringUtils.isBlank(to)) {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery())
                    .sort(new FieldSortBuilder(Constant.TIME_STAMP_KEYWORD).order(SortOrder.DESC));
        } else {
            if (!StringUtils.isBlank(name)) {
                boolQueryBuilder.must(QueryBuilders.wildcardQuery(Constant.NAME_KEYWORD,
                        Constant.STAR + name + Constant.STAR));
            }
            if (!StringUtils.isBlank(application)) {
                boolQueryBuilder.must(QueryBuilders.wildcardQuery(Constant.APP_KEYWORD,
                        Constant.STAR + application + Constant.STAR));
            }
            if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(Constant.START_TIME_KEYWORD)
                        .gte(simpleDateFormat.parse(from).getTime())
                        .lte(simpleDateFormat.parse(to).getTime()));
            }
            searchSourceBuilder.query(boolQueryBuilder).sort(new FieldSortBuilder(Constant.TIME_STAMP_KEYWORD)
                    .order(SortOrder.DESC));
        }

        SearchResponse searchResponse = search(Constant.RECORD_JOB_INDEX, searchSourceBuilder);
        RecordJobs jobs = new RecordJobs();
        jobs.setTotal(searchResponse.getHits().getTotalHits().value);
        jobs.setJobs(getJobEntities(searchResponse, RecordJobEntity.class));
        return jobs;
    }

    /**
     * 查询即将进行的录制任务
     */
    @Override
    public RecordJobs getComingRecordJob() throws IOException {
        SearchRequest searchRequest = new SearchRequest(Constant.RECORD_JOB_INDEX);
        searchRequest.scroll(SCROLL);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(Constant.SCROLL_SIZE);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(Constant.STATUS_KEYWORD, Constant.PENDING_STATUS))
                .filter(QueryBuilders.rangeQuery(Constant.START_TIME_KEYWORD)
                        .gte(new Date().getTime() - Constant.TIME_SCAN_RANGE).lte(new Date().getTime()));
        searchSourceBuilder.query(boolQueryBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest.source(searchSourceBuilder),
                RequestOptions.DEFAULT);
        RecordJobs jobs = new RecordJobs();
        jobs.setTotal(searchResponse.getHits().getTotalHits().value);
        jobs.setJobs(getJobEntities(searchResponse, RecordJobEntity.class));
        return jobs;
    }

    /**
     * 根据录制任务ID查询索引列表
     */
    @Override
    public List<String> getIndexList(String jobId) throws IOException {
        GetIndexRequest request = new GetIndexRequest(jobId + Constant.STAR);
        GetIndexResponse response = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT);
        return Arrays.asList(response.getIndices());
    }

    /**
     * 查询索引是否存在
     */
    @Override
    public boolean checkIndexExistence(String index) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 往ES中写入数据
     */
    @Override
    public void insert(String index, Object data) throws IOException {
        IndexRequest request = new IndexRequest(index);
        request.source(JSON.toJSONString(data), XContentType.JSON);
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除录制任务
     */
    @Override
    public void deleteRecordJob(String jobId) throws IOException {
        delete(Constant.RECORD_JOB_INDEX, Constant.JOB_ID_KEYWORD, jobId);
    }

    /**
     * 删除回放任务
     */
    @Override
    public void deleteReplayJob(String jobId) throws IOException {
        delete(Constant.REPLAY_JOB_INDEX, Constant.JOB_ID_KEYWORD, jobId);
    }

    /**
     * 删除回放子任务
     */
    @Override
    public void deleteSubReplayJob(String jobId) throws IOException {
        delete(Constant.REPLAY_SUB_JOB_INDEX, Constant.JOB_ID_KEYWORD, jobId);
    }

    /**
     * 根据回放任务ID查询回放任务
     */
    @Override
    public ReplayJobEntity getReplayJob(String jobId) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(Constant.JOB_ID_KEYWORD, jobId));
        searchSourceBuilder.query(boolQueryBuilder).sort(new FieldSortBuilder(Constant.TIME_STAMP_KEYWORD)
                .order(SortOrder.DESC));
        SearchResponse searchResponse = search(Constant.REPLAY_JOB_INDEX, searchSourceBuilder);

        ReplayJobEntity replayJobEntity = null;
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits != null && hits.length == 1) {
            String sourceAsString = hits[0].getSourceAsString();
            replayJobEntity = JSON.parseObject(sourceAsString, ReplayJobEntity.class);
        }
        return replayJobEntity;
    }

    /**
     * 条件查询回放任务列表
     */
    @Override
    public ReplayJobs getReplayJobList(String name, String application) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isBlank(name) && StringUtils.isBlank(application)) {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery())
                    .sort(new FieldSortBuilder(Constant.TIME_STAMP_KEYWORD).order(SortOrder.DESC));
        } else {
            if (!StringUtils.isBlank(name)) {
                boolQueryBuilder.must(QueryBuilders.wildcardQuery(Constant.NAME_KEYWORD,
                        Constant.STAR + name + Constant.STAR));
            }
            if (!StringUtils.isBlank(application)) {
                boolQueryBuilder.must(QueryBuilders.wildcardQuery(Constant.APP_KEYWORD,
                        Constant.STAR + application + Constant.STAR));
            }
            searchSourceBuilder.query(boolQueryBuilder).sort(new FieldSortBuilder(Constant.TIME_STAMP_KEYWORD)
                    .order(SortOrder.DESC));
        }

        SearchResponse searchResponse = search(Constant.REPLAY_JOB_INDEX, searchSourceBuilder);
        ReplayJobs jobs = new ReplayJobs();
        jobs.setTotal(searchResponse.getHits().getTotalHits().value);
        jobs.setJobs(getJobEntities(searchResponse, ReplayJobEntity.class));
        return jobs;
    }

    /**
     * 终止回放任务
     */
    @Override
    public void stopReplayJob(String jobId) throws IOException {
        updateReplayJob(jobId, Constant.STATUS_KEYWORD, Constant.STOPPED_STATUS);
    }

    /**
     * 查询回放worker的job信息
     */
    @Override
    public Map<String, String> getJobByWorker(String worker) throws IOException {
        SearchRequest searchRequest = new SearchRequest(Constant.REPLAY_SUB_JOB_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(Constant.REPLAY_WORKER_KEYWORD, worker))
                .must(QueryBuilders.termQuery(Constant.STATUS_KEYWORD, Constant.RUNNING_STATUS));
        searchSourceBuilder.query(boolQueryBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest.source(searchSourceBuilder),
                RequestOptions.DEFAULT);

        Map<String, String> map = new HashMap<>();
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits != null && hits.length == 1) {
            Map<String, Object> sourceAsMap = hits[0].getSourceAsMap();
            String jobId = (String) sourceAsMap.get(Constant.JOB_ID_KEYWORD);
            String index = (String) sourceAsMap.get(Constant.RECORD_INDEX_KEYWORD);
            map.put(Constant.JOB_ID_KEYWORD, jobId);
            map.put(Constant.RECORD_INDEX_KEYWORD, index);
        }
        return map;
    }

    /**
     * 检查es某个字段值的唯一性
     */
    @Override
    public boolean checkDocExistenceByKeyword(String index, String key, Object value) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(key, value));
        searchSourceBuilder.query(boolQueryBuilder);
        SearchResponse searchResponse = search(index, searchSourceBuilder);
        return searchResponse.getHits().getHits().length != 0;
    }

    /**
     * 检查同一应用下录制任务时间是否冲突
     */
    @Override
    public boolean checkPeriodValidation(String application, Date start, Date end) throws IOException {
        SearchRequest searchRequest = new SearchRequest(Constant.RECORD_JOB_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(Constant.APP_KEYWORD, application))
                .should(QueryBuilders.termQuery(Constant.STATUS_KEYWORD, Constant.PENDING_STATUS))
                .should(QueryBuilders.termQuery(Constant.STATUS_KEYWORD, Constant.RUNNING_STATUS))
                .filter(QueryBuilders.boolQuery()
                        .should(QueryBuilders.rangeQuery(Constant.START_TIME_KEYWORD)
                                .gte(start.getTime()).lte(end.getTime()))
                        .should(QueryBuilders.rangeQuery(Constant.END_TIME_KEYWORD)
                                .gte(start.getTime()).lte(end.getTime()))
                        .should(QueryBuilders.boolQuery()
                                .must(QueryBuilders.rangeQuery(Constant.START_TIME_KEYWORD).gte(start.getTime()))
                                .must(QueryBuilders.rangeQuery(Constant.END_TIME_KEYWORD).lte(end.getTime()))));

        searchSourceBuilder.query(boolQueryBuilder).sort(new FieldSortBuilder(Constant.TIME_STAMP_KEYWORD)
                .order(SortOrder.DESC));
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest.source(searchSourceBuilder),
                RequestOptions.DEFAULT);
        return searchResponse.getHits().getHits().length != 0;
    }

    /**
     * 更新录制任务es字段
     */
    @Override
    public void updateRecordJob(String jobId, String key, String value) throws IOException {
        QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.JOB_ID_KEYWORD, jobId);
        updateField(Constant.RECORD_JOB_INDEX, queryBuilder, key, value);
    }

    /**
     * 更新回放任务es字段
     */
    @Override
    public void updateReplayJob(String jobId, String key, String value) throws IOException {
        QueryBuilder queryBuilder = QueryBuilders.termQuery(Constant.JOB_ID_KEYWORD, jobId);
        updateField(Constant.REPLAY_JOB_INDEX, queryBuilder, key, value);
    }

    /**
     * 更新es字段
     *
     * @param index        更新es索引
     * @param queryBuilder 查询体
     * @param key          字段名称
     * @param value        更新内容
     */
    public void updateField(String index, QueryBuilder queryBuilder, String key, String value) throws IOException {
        UpdateByQueryRequest request = new UpdateByQueryRequest(index);
        request.setQuery(QueryBuilders.boolQuery().must(queryBuilder));
        request.setScript(new Script("ctx._source['" + key + "']='" + value + "'"));
        request.setRefresh(true);
        restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
    }

    /**
     * ES查询接口
     *
     * @param index               es索引
     * @param searchSourceBuilder 查询条件
     * @return 返回查询结果
     */
    public SearchResponse search(String index, SearchSourceBuilder searchSourceBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.scroll(SCROLL);
        searchSourceBuilder.size(Constant.SCROLL_SIZE);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest.source(searchSourceBuilder),
                RequestOptions.DEFAULT);
        return searchResponse;
    }

    /**
     * 删除es doc
     *
     * @param index es索引
     * @param key   es字段
     * @param value 字段内容
     */
    public void delete(String index, String key, String value) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termQuery(key, value)));
        request.setRefresh(true);
        restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
    }

    /**
     * ES分页查询接口
     *
     * @param index   es索引
     * @param keyword es字段
     * @param target  es查询条件
     * @param page    分页数
     * @param size    分页大小
     * @return 查询结果
     */
    public SearchResponse searchByPagination(String index, String keyword, Object target, int page, int size)
            throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from((page - 1) * size);
        searchSourceBuilder.size(size);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(keyword, target));
        searchSourceBuilder.query(boolQueryBuilder).sort(new FieldSortBuilder(Constant.TIME_STAMP_KEYWORD)
                .order(SortOrder.DESC));
        SearchRequest searchRequest = new SearchRequest(index);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest.source(searchSourceBuilder),
                RequestOptions.DEFAULT);
        return searchResponse;
    }

    /**
     * 从ES查询结果过滤出所需信息
     */
    private <T> List<T> getJobEntities(SearchResponse searchResponse, Class<T> clazz) throws IOException {
        String scrollId = searchResponse.getScrollId();
        List<T> jobList = new ArrayList<>();
        SearchHit[] hits = searchResponse.getHits().getHits();
        while (hits != null && hits.length > 0) {
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                T jobEntity = JSON.parseObject(sourceAsString, clazz);
                jobList.add(jobEntity);
            }
            SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
            searchScrollRequest.scroll(SCROLL);
            SearchResponse response = restHighLevelClient.searchScroll(searchScrollRequest, RequestOptions.DEFAULT);
            scrollId = response.getScrollId();
            hits = response.getHits().getHits();
        }
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        return jobList;
    }

    /**
     * 查询定量条数并返回
     *
     * @param <T>            返回类名
     * @param clazz          结果类
     * @param searchResponse 返回结果
     * @return 返回类的list
     */
    public <T> List<T> getRecordResultLimit(SearchResponse searchResponse, Class<T> clazz) throws IOException {
        List<T> jobList = new ArrayList<>();
        int cnt = 0;
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            T jobEntity = JSON.parseObject(sourceAsString, clazz);
            jobList.add(jobEntity);
            cnt++;
            if (cnt >= Constant.MAX_FINDER) {
                break;
            }
        }
        return jobList;
    }
}
