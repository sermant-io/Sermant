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

import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSourceAggregate;
import com.huawei.flowrecordreplay.console.datasource.entity.stresstest.FlowReplayMetric;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.fastjson.JSON;

import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 压力测试数据处理逻辑
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-21
 */
@Service
public class StressTestResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StressTestResultService.class);

    /**
     * 响应时间字段
     */
    private static final String RESPONSE_TIME = "responseTime";

    @Autowired
    private EsDataSource esDataSource;

    @Autowired
    private EsDataSourceAggregate esDataSourceAggregate;

    /**
     * 获取响应时间的统计数据
     *
     * @param replayJobId 回放任务ID
     * @return 响应时间和响应时间计数的map
     */
    public Map<String, Long> getResponseTimeStatistics(String replayJobId) {
        Map<String, Long> responseTimeStatistics = new LinkedHashMap<>();
        try {
            List<Terms.Bucket> bucketList =
                esDataSourceAggregate.getBuckets(Constant.REPLAY_RESULT_PREFIX + replayJobId, RESPONSE_TIME);
            bucketList.sort(Comparator.comparingInt(object -> object.getKeyAsNumber().intValue()));
            for (Terms.Bucket bucket : bucketList) {
                responseTimeStatistics.put(bucket.getKeyAsString() + "ms", bucket.getDocCount());
            }
        } catch (IOException ioException) {
            LOGGER.error("Get response time statistics error:{}", ioException.getMessage());
        } finally {
            return responseTimeStatistics;
        }
    }

    /**
     * 获取对应任务的回访节点信息
     *
     * @param replayJobId 回放任务id
     * @return 任务阶段的回放节点指标列表
     */
    public List<FlowReplayMetric> getFlowReplayMetricList(String replayJobId) {
        List<FlowReplayMetric> flowReplayMetrics = new ArrayList<>();
        try {
            List<String> replayMetrics = esDataSource.searchByKey(Constant.REPLAY_METRIC, "replayJobId", replayJobId);
            for (String replayMetric : replayMetrics) {
                flowReplayMetrics.add(JSON.parseObject(replayMetric, FlowReplayMetric.class));
            }
            Collections.sort(flowReplayMetrics, FlowReplayMetric::compareTo);
        } catch (IOException ioException) {
            LOGGER.error("Get replay metric error:{}", ioException.getMessage());
        } finally {
            return flowReplayMetrics;
        }
    }
}