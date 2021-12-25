/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */
package com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 * 下发引流压测字段
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-12-10
 */
@Getter
@Setter
public class NgrinderJob {

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务应用
     */
    private String application;

    /**
     * 录制任务id
     */
    private String recordJobId;

    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 各接口百分比
     */
    private Map<String, Integer> precentage;
}
