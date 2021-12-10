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

package com.huawei.flowrecordreplay.console.service;

import com.huawei.flowrecordreplay.console.datasource.elasticsearch.ElasticsearchJobStorageImpl;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSourceAggregate;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordInterfaceCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordResultCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordResultEntity;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.fastjson.JSON;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 对录制的结果进行处理
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-06-05
 */
@Service
public class RecordResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordResultService.class);

    /**
     * 录制数据methodName字段
     */
    private static final String METHOD_NAME = "methodName";

    /**
     * 录制数据时间戳字段
     */
    private static final String TIME_STAMP = "timestamp";

    /**
     * 录制结果表index前缀
     */
    private static final String PREFIX_SUBCALL = "subcall_";

    /**
     * 录制数据traceId字段
     */
    private static final String TRACE_ID = "traceId";

    @Autowired
    EsDataSourceAggregate esDataSourceAggregate;

    @Autowired
    EsDataSource esDataSource;

    @Autowired
    ElasticsearchJobStorageImpl elasticsearchJobStorage;

    /**
     * 录制结果的统计
     *
     * @param jobId 回放任务id
     * @return RecordResultCountEntity 录制结果的统计
     */
    public RecordResultCountEntity getRecordOverview(String jobId) throws IOException {
        RecordResultCountEntity recordResultCountEntity = new RecordResultCountEntity();
        recordResultCountEntity.setJobId(jobId);
        recordResultCountEntity.setRecordInterfaceCount(esDataSourceAggregate.recordInterfaceCount(jobId));
        for (RecordInterfaceCountEntity recordInterfaceCountEntity
            : recordResultCountEntity.getRecordInterfaceCount()) {
            recordResultCountEntity.setRecordTotalCount(recordResultCountEntity.getRecordTotalCount()
                + recordInterfaceCountEntity.getTotal());
        }
        return recordResultCountEntity;
    }

    /**
     * 获取录制任务的全部子调用接口
     *
     * @param jobId 录制任务id
     * @return 返回子调用接口列表
     */
    public List<String> getSubCallMethods(String jobId) {
        try {
            return esDataSourceAggregate.getAllSubCallMethod(jobId);
        } catch (IOException ioException) {
            LOGGER.error("Get subCallMethods error:{}", ioException.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 通过关键字段查找录制数据
     *
     * @param jobId     录制任务id
     * @param method    接口名称
     * @param traceId   链路Id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 根据关键字段筛选的详细比对结果列表
     */
    public RecordResultEntity getRecordResult(String jobId, String method, String traceId,
                                              String startTime, String endTime) {
        RecordResultEntity recordResultEntity = new RecordResultEntity();
        List<RecordEntity> recordEntities = new ArrayList<>();
        List<String> recordResultString;
        int cnt = 0;
        try {
            if (StringUtils.isNotBlank(method)) {
                recordResultString = esDataSource.searchByKeyEnd(jobId + Constant.STAR, METHOD_NAME, method);
                cnt = limitCount(recordResultString, recordEntities);
                recordResultEntity.setRecordEntities(recordEntities);
                recordResultEntity.setTotal(cnt);
                return recordResultEntity;
            }

            if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(TIME_STAMP)
                    .gte(simpleDateFormat.parse(startTime).getTime())
                    .lte(simpleDateFormat.parse(endTime).getTime()));
                searchSourceBuilder.query(boolQueryBuilder).sort(new FieldSortBuilder(TIME_STAMP).order(SortOrder.ASC));
                SearchResponse searchResponse = elasticsearchJobStorage
                    .search(jobId + Constant.STAR, searchSourceBuilder);

                recordResultEntity.setRecordEntities(elasticsearchJobStorage.getRecordResultLimit(searchResponse,
                    RecordEntity.class));
                recordResultEntity.setTotal(recordResultEntity.getRecordEntities().size());
                return recordResultEntity;
            }

            if (StringUtils.isNotBlank(traceId)) {
                recordResultString = esDataSource.searchByKeyEnd(jobId + Constant.STAR, TRACE_ID, traceId);
                cnt = limitCount(recordResultString, recordEntities);
                recordResultString = esDataSource.searchByKeyEnd(PREFIX_SUBCALL + jobId, TRACE_ID, traceId);
                cnt += limitCount(recordResultString, recordEntities);
                recordResultEntity.setRecordEntities(recordEntities);
                recordResultEntity.setTotal(cnt);
                return recordResultEntity;
            }

            recordResultString = esDataSource.getAllDataEnd(jobId + Constant.STAR);
            cnt = limitCount(recordResultString, recordEntities);
            recordResultEntity.setRecordEntities(recordEntities);
            recordResultEntity.setTotal(cnt);
        } catch (IOException | ParseException ioExcetion) {
            LOGGER.error("Get record result from es error , {}", ioExcetion.getMessage());
        }
        return recordResultEntity;
    }

    /**
     * 前端展示条数限制
     *
     * @param recordResultString 录制数据级
     * @param recordEntities     录制数据展示内容
     */
    private int limitCount(List<String> recordResultString, List<RecordEntity> recordEntities) {
        int cnt = 0;
        for (String str : recordResultString) {
            recordEntities.add(JSON.parseObject(str, RecordEntity.class));
            cnt++;
            if (cnt >= Constant.MAX_FINDER) {
                break;
            }
        }
        return cnt;
    }
}