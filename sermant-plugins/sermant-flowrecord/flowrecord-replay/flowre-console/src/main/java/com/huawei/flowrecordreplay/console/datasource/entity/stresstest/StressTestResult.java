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