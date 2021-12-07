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

package com.huawei.flowrecordreplay.console.datasource.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 录制子任务对象
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-03-23
 */
@Getter
@Setter
public class SubReplayJobEntity {
    /**
     * 任务ID
     */
    private String jobId;

    /**
     * 需要回放的录制任务ID
     */
    private String recordJobId;

    /**
     * 应用名称
     */
    private String application;

    /**
     * 录制子任务索引
     */
    private String recordIndex;

    /**
     * 回放时截取的录制任务的时间起点
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date from;

    /**
     * 回放时截取的录制任务的时间终点
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date to;

    /**
     * 回放环境：1. dubbo应用场景为注册中心地址；2. http场景为域名或ip
     */
    private String address;

    /**
     * 任务下发的时间戳
     */
    private long timeStamp;

    /**
     * 回放时参数修改规则
     */
    private Map<String, List<ModifyRuleEntity>> modifyRule;

    /**
     * 压力回放类型
     */
    private String stressTestType;

    /**
     * 基线吞吐量
     */
    private int baselineThroughPut;

    /**
     * 最大线程数
     */
    private int maxThreadCount;

    /**
     * 最大响应时间
     */
    private int maxResponseTime;

    /**
     * 最小成功率
     */
    private int minSuccessRate;
}