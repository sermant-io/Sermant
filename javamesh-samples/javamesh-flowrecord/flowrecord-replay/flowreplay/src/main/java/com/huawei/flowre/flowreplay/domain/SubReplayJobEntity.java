/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 回放子任务的信息
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-16
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
     * 回放任务应该回放的录制索引
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
