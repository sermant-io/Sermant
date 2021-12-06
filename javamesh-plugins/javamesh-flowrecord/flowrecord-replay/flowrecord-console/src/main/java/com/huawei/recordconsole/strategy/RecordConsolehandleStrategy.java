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

package com.huawei.recordconsole.strategy;

import static java.lang.Math.max;

import com.huawei.recordconsole.config.CommonConfig;
import com.huawei.recordconsole.desensitization.DataDesensitize;
import com.huawei.recordconsole.elasticsearch.ElasticSearchIndexCreator;
import com.huawei.recordconsole.entity.EntryRecordEntity;
import com.huawei.recordconsole.entity.Recorder;
import com.huawei.recordconsole.entity.SubcallEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 该类是ITopicHandleStrategy的实现类，专门用于处理从kafka拉取出来的flowrecord数据
 *
 * @author lihongjiang
 * @since 2021-02-19
 */

@Component("request")
public class RecordConsolehandleStrategy implements InterfaceTopicHandleStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordConsolehandleStrategy.class);
    @Autowired
    private ElasticSearchIndexCreator elasticSearchimpl;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DataDesensitize dataDesensitize;

    /**
     * 重写ITopicHandleStrategy的handleRecordByTopic()方法，用于处理从kafka拉取出来的request数据
     *
     * @param records 表示数据集
     */
    @Override
    public void handleRecordByTopic(List<ConsumerRecord<String, String>> records) throws IOException {
        if (records == null) {
            return;
        }

        for (ConsumerRecord<String, String> record: records) {
            String index = generateIndex(record);
            // convert to struct

            Recorder res = JSON.parseObject(record.value(), Recorder.class);

            if (CommonConfig.DUBBO.equals(res.getAppType())) {
                try {
                    // 数据脱敏流程
                    res = dataDesensitize.dubboDesensitize(res);
                } catch (Exception e) {
                    LOGGER.info("[flowrecord]: fail to desensitize the record");
                    break;
                }
            }

            try {
                if (res.isEntry()) {
                    EntryRecordEntity entryRecordEntity = new EntryRecordEntity();
                    phaseEntry(entryRecordEntity, res);

                    // store to es with request
                    IndexRequest indexRequest = new IndexRequest(index);
                    indexRequest.source(JSON.toJSONString(entryRecordEntity,
                            SerializerFeature.WriteMapNullValue), XContentType.JSON);
                    indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
                    restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                } else {
                    SubcallEntity subcallEntity = new SubcallEntity();
                    phaseSubcall(subcallEntity, res);

                    // store to es with request
                    IndexRequest indexRequest = new IndexRequest(index);
                    indexRequest.source(JSON.toJSONString(subcallEntity,
                            SerializerFeature.WriteMapNullValue), XContentType.JSON);
                    indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
                    restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                }
            } catch (Exception e) {
                LOGGER.info("[flowrecord]: cannot store the record");
            }
        }
    }

    private String generateIndex(ConsumerRecord<String, String> record) throws IOException {
        Recorder recorder = JSON.parseObject(record.value(), Recorder.class);
        String ret = recorder.getJobId();
        String index;
        int max = 0;

        if (recorder.isEntry()) {
            // 获取所有index列表并筛选同样jobid开头的并且截取后面数字，取最大值
            if (!elasticSearchimpl.checkIndexExistence(recorder.getJobId() + CommonConfig.ES_STORAGE_BEGINNING)) {
                elasticSearchimpl.createRecordJobIndex(recorder.getJobId() + CommonConfig.ES_STORAGE_BEGINNING);
            }
            GetIndexRequest request = new GetIndexRequest(ret + "*");
            GetIndexResponse response = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT);
            for (String indices : Arrays.asList(response.getIndices())) {
                if (indices.split(CommonConfig.ES_INDEX_SEPARATOR).length == 2) {
                    max = max(max, Integer.parseInt(indices.split(CommonConfig.ES_INDEX_SEPARATOR)[1]));
                }
            }
            String num = String.format(CommonConfig.ES_FORMAT_ZERO + CommonConfig.ES_FORMAT_NUM + "d", max);
            index = ret + CommonConfig.ES_INDEX_SEPARATOR + num;
            SearchRequest searchRequest = new SearchRequest(ret + CommonConfig.ES_INDEX_SEPARATOR + num);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Long hits = searchResponse.getHits().getTotalHits().value;
            if (hits >= CommonConfig.ES_SAVING_DOC_NUM) {
                index = ret + CommonConfig.ES_INDEX_SEPARATOR
                        + String.format(CommonConfig.ES_FORMAT_ZERO + CommonConfig.ES_FORMAT_NUM + "d", max + 1);
                if (!elasticSearchimpl.checkIndexExistence(index)) {
                    elasticSearchimpl.createRecordJobIndex(index);
                }
            }
        } else {
            // 获取所有index列表并筛选同样subcalljobid开头的并且截取后面数字，取最大值
            index = "subcall_" + recorder.getJobId();
            if (!elasticSearchimpl.checkIndexExistence(index)) {
                elasticSearchimpl.createSubcallIndex(index);
            }
        }
        return index;
    }

    private void phaseEntry(EntryRecordEntity entryRecordEntity, Recorder recorder) {
        entryRecordEntity.setJobId(recorder.getJobId());
        entryRecordEntity.setAppType(recorder.getAppType());
        entryRecordEntity.setMethodName(recorder.getMethodName());
        entryRecordEntity.setRequestBody(recorder.getRequestBody());
        entryRecordEntity.setRequestClass(recorder.getRequestClass());
        entryRecordEntity.setResponseBody(recorder.getResponseBody());
        entryRecordEntity.setResponseClass(recorder.getResponseClass());
        entryRecordEntity.setTraceId(recorder.getTraceId());
	    entryRecordEntity.setTimestamp(recorder.getTimestamp());
    }

    private void phaseSubcall(SubcallEntity subcallEntity, Recorder recorder) {
        subcallEntity.setSubCallKey(recorder.getSubCallKey());
        subcallEntity.setJobId(recorder.getJobId());
        subcallEntity.setAppType(recorder.getAppType());
        subcallEntity.setMethodName(recorder.getMethodName());
        subcallEntity.setRequestBody(recorder.getRequestBody());
        subcallEntity.setRequestClass(recorder.getRequestClass());
        subcallEntity.setResponseBody(recorder.getResponseBody());
        subcallEntity.setResponseClass(recorder.getResponseClass());
        subcallEntity.setTraceId(recorder.getTraceId());
	    subcallEntity.setTimestamp(recorder.getTimestamp());
        subcallEntity.setSubCallCount(recorder.getSubCallCount());
    }
}
