/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity.stresstest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 压力测试结果数据
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-21
 */
@Getter
@Setter
public class StressTestResult {
    /**
     * 回放任务Id
     */
    private String replayJobId;

    /**
     * 测试类型 baselineTest inflectionPointTest
     */
    private String testType;

    /**
     * 响应时间统计数据
     */
    private Map<String, Long> responseTimeStatistics;

    /**
     * 回放节点指标
     */
    private List<FlowReplayMetric> flowReplayMetricList;
}