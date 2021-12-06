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

package com.huawei.flowrecordreplay.console.service;

import com.huawei.flowrecordreplay.console.datasource.elasticsearch.ElasticSearchIndicesInit;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSourceAggregate;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.IgnoreFieldEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayInterfaceCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultDetailEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultEntity;
import com.huawei.flowrecordreplay.console.datasource.zookeeper.ZookeeperUtil;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对回放的结果进行处理
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-06
 */
@Service
public class ReplayResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayResultService.class);

    /**
     * 回放结果表index前缀
     */
    private static final String REPLAY_RESULT_PREFIX = "replay_result_";

    /**
     * 忽略字段表index
     */
    private static final String IGNORE_FIELDS_INDEX = "ignore_fields";

    /**
     * 结果比对中 详细结果比对的字段
     */
    private static final String FIELD_COMPARE = "fieldCompare";

    private static final String METHOD_KEYWORD = "method";

    private static final String TRACE_KEYWORD = "traceId";

    private static final String RETURN_BLANK = "";

    private static final String RE_COMPARE_PATH = "/re_compare/";

    @Autowired
    EsDataSourceAggregate esDataSourceAggregate;

    @Autowired
    EsDataSource esDataSource;

    @Autowired
    CuratorFramework zkClient;

    @Autowired
    ElasticSearchIndicesInit elasticSearchIndicesInit;

    /**
     * 回放结果的统计
     *
     * @param jobId 回放任务id
     * @return ReplayResultCountEntity 回放结果的统计
     */
    public ReplayResultCountEntity getReplayOverview(String jobId) {
        ReplayResultCountEntity replayResultCountEntity = new ReplayResultCountEntity();
        replayResultCountEntity.setJobId(jobId);
        try {
            replayResultCountEntity.setReplayInterfaceCount(esDataSourceAggregate.replayInterfaceCount(jobId));
            for (ReplayInterfaceCountEntity replayInterfaceCountEntity
                : replayResultCountEntity.getReplayInterfaceCount()) {
                replayResultCountEntity.setReplayTotal(replayResultCountEntity.getReplayTotal()
                    + replayInterfaceCountEntity.getTotal());
                replayResultCountEntity.setReplaySuccessCount(replayResultCountEntity.getReplaySuccessCount()
                    + replayInterfaceCountEntity.getSuccessCount());
                replayResultCountEntity.setReplayFailureCount(replayResultCountEntity.getReplayFailureCount()
                    + replayInterfaceCountEntity.getFailureCount());
            }

            // 获取响应结果的统计
            Map<String, Long> statusCodeStatistics = new HashMap<>();
            List<Terms.Bucket> bucketList
                = esDataSourceAggregate.getBuckets(Constant.REPLAY_RESULT_PREFIX + jobId, "statusCode");
            for (Terms.Bucket bucket : bucketList) {
                statusCodeStatistics.put(bucket.getKeyAsString(), bucket.getDocCount());
            }
            replayResultCountEntity.setStatusCodeStatistics(statusCodeStatistics);
        } catch (IOException ioException) {
            LOGGER.error("Replay result get count data from es error , {}", ioException.getMessage());
        }
        try {
            String reCompareStatus = ZookeeperUtil.getData(RE_COMPARE_PATH + jobId, zkClient);
            if (!RETURN_BLANK.equals(reCompareStatus)) {
                replayResultCountEntity.setReCompareStatus(true);
            }
        } catch (Exception e) {
            LOGGER.error("Get re-compare status error , {}", e.getMessage());
        }
        return replayResultCountEntity;
    }

    /**
     * 通过接口名查找回放比对结果
     *
     * @param jobId   回放任务id
     * @param method  接口名称
     * @param correct 回放成功或者失败
     * @return 根据接口名称筛选的详细比对结果列表
     */
    public List<JSONObject> getReplayResultCompare(String jobId, String method, String correct) {
        try {
            List<JSONObject> replayResults = new ArrayList<>();
            List<String> replayResultString;
            if (StringUtils.isBlank(method)) {
                replayResultString = esDataSource.getAllData(REPLAY_RESULT_PREFIX + jobId);
            } else {
                replayResultString = esDataSource.searchByKey(REPLAY_RESULT_PREFIX + jobId,
                    METHOD_KEYWORD, method);
            }
            if (!StringUtils.isBlank(correct)) {
                for (String str : replayResultString) {
                    JSONObject jsonObject = JSON.parseObject(str);
                    if (jsonObject.get("compareResult").toString().equals(correct)) {
                        jsonObject.remove(FIELD_COMPARE);
                        replayResults.add(jsonObject);
                    }
                }
            } else {
                for (String str : replayResultString) {
                    JSONObject jsonObject = JSON.parseObject(str);
                    jsonObject.remove(FIELD_COMPARE);
                    replayResults.add(jsonObject);
                }
            }
            return replayResults;
        } catch (IOException ioException) {
            LOGGER.error("Get replay result from es error , {}", ioException.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 忽略字段存储服务 如果已经存在
     *
     * @param ignoreFieldEntity 前端传回的忽略字段
     * @return 是否上传忽略字段成功
     */
    public boolean ignoreFiled(IgnoreFieldEntity ignoreFieldEntity) {
        try {
            String docId = esDataSource.getDocId(IGNORE_FIELDS_INDEX, METHOD_KEYWORD, ignoreFieldEntity.getMethod());
            if (RETURN_BLANK.equals(docId)) {
                esDataSource.addData(IGNORE_FIELDS_INDEX, ignoreFieldEntity);
            } else {
                esDataSource.update(IGNORE_FIELDS_INDEX, docId, ignoreFieldEntity);
            }
            return true;
        } catch (IOException ioException) {
            LOGGER.error("Update ignore field to es error , {}", ioException.getMessage());
        }
        return false;
    }

    /**
     * 获取一条trace的详细结果比对
     *
     * @param jobId   回放任务的id
     * @param traceId 请求的traceId
     * @param method  请求的接口信息
     * @return 返回一条trace请求的详细结果比对信息
     */
    public ReplayResultDetailEntity getReplayResultDetail(String jobId, String traceId, String method) {
        ReplayResultDetailEntity replayResultDetailEntity = new ReplayResultDetailEntity();
        replayResultDetailEntity.setJobId(jobId);
        replayResultDetailEntity.setTraceId(traceId);
        replayResultDetailEntity.setMethod(method);
        List<String> replayResults = null;
        try {
            replayResults = esDataSource.searchByKey(REPLAY_RESULT_PREFIX + jobId, TRACE_KEYWORD, traceId);
        } catch (IOException ioException) {
            LOGGER.error("Get replay result detail from es error , {}", ioException.getMessage());
        }
        if (replayResults != null && replayResults.size() > 0) {
            JSONObject replayResult = JSON.parseObject(replayResults.get(0));
            replayResultDetailEntity.setFieldsCompare(replayResult.getJSONArray(FIELD_COMPARE));
        }
        List<String> fieldsIgnores = null;
        try {
            fieldsIgnores = esDataSource.searchByKey(IGNORE_FIELDS_INDEX, METHOD_KEYWORD, method);
        } catch (IOException ioException) {
            LOGGER.error("Get replay fields ignore from es error , {}", ioException.getMessage());
        }
        if (fieldsIgnores != null && fieldsIgnores.size() > 0) {
            JSONObject fieldsIgnore = JSON.parseObject(fieldsIgnores.get(0));
            replayResultDetailEntity.setFieldsIgnore(fieldsIgnore.getJSONArray("fields"));
        }
        return replayResultDetailEntity;
    }

    /**
     * 重新进行结果比对
     *
     * @param jobId  回放任务id
     * @param method 接口名
     */
    @Async
    public void reCompare(String jobId, String method) {
        LOGGER.info("Start re compare!");
        try {
            try {
                ZookeeperUtil.setData(RE_COMPARE_PATH + jobId, "comparing", zkClient);
            } catch (Exception e) {
                LOGGER.error("Re compare set status to zk error , {}", e.getMessage());
            }
            List<String> replayResultList = esDataSource.getAllData(REPLAY_RESULT_PREFIX + jobId);
            for (String str : replayResultList) {
                ReplayResultEntity replayResultEntity = JSON.parseObject(str, ReplayResultEntity.class);

                // 接口对应 重新比对结果
                if (replayResultEntity.getMethod().equals(method)) {
                    boolean reCompareStatus = compare(jobId, replayResultEntity);
                    LOGGER.info(replayResultEntity.getTraceId() + ":" + "re compare status {}", reCompareStatus);
                }
            }
            try {
                zkClient.delete().forPath(RE_COMPARE_PATH + jobId);
            } catch (Exception e) {
                LOGGER.error("Re compare delete status from zk error , {}", e.getMessage());
            }
        } catch (IOException ioException) {
            LOGGER.error("Re compare get replay result from es error , {}", ioException.getMessage());
        }
    }

    /**
     * 重新进行结果比对
     *
     * @param jobId 回放任务id
     */
    @Async
    public void reCompare(String jobId) {
        LOGGER.info("Start re compare!");
        try {
            try {
                ZookeeperUtil.setData(RE_COMPARE_PATH + jobId, "comparing", zkClient);
            } catch (Exception e) {
                LOGGER.error("Re compare set status to zk error , {}", e.getMessage());
            }
            List<String> replayResultList = esDataSource.getAllData(REPLAY_RESULT_PREFIX + jobId);
            for (String str : replayResultList) {
                ReplayResultEntity replayResultEntity = JSON.parseObject(str, ReplayResultEntity.class);
                boolean reCompareStatus = compare(jobId, replayResultEntity);
                LOGGER.info(replayResultEntity.getTraceId() + ":" + "re compare status {}", reCompareStatus);
            }
            try {
                zkClient.delete().deletingChildrenIfNeeded().forPath(RE_COMPARE_PATH + jobId);
            } catch (Exception e) {
                LOGGER.error("Delete re compare status from zk error , {}", e.getMessage());
            }
        } catch (IOException ioException) {
            LOGGER.error("Re compare get replay result from es error , {}", ioException.getMessage());
        }
    }

    public boolean compare(String jobId, ReplayResultEntity replayResultEntity) {
        String ignoreFiledString;
        try {
            ignoreFiledString = esDataSource.getOne(IGNORE_FIELDS_INDEX, METHOD_KEYWORD,
                replayResultEntity.getMethod());
        } catch (IOException ioException) {
            LOGGER.error("Re compare get ignore fields from es error , {}", ioException.getMessage());
            return false;
        }
        if (!RETURN_BLANK.equals(ignoreFiledString)) {
            IgnoreFieldEntity ignoreFieldEntity = JSON.parseObject(ignoreFiledString, IgnoreFieldEntity.class);
            replayResultEntity.reCompare(ignoreFieldEntity);
            try {
                String docId = esDataSource.getDocId(REPLAY_RESULT_PREFIX + jobId,
                    TRACE_KEYWORD, replayResultEntity.getTraceId());
                if (!RETURN_BLANK.equals(docId)) {
                    esDataSource.update(REPLAY_RESULT_PREFIX + jobId, docId, replayResultEntity);
                    return true;
                }
            } catch (IOException ioException) {
                LOGGER.error("Re compare update replay result to es error , {}", ioException.getMessage());
                return false;
            }
        }
        LOGGER.info("Ignore Fields is not exist!");
        return false;
    }
}