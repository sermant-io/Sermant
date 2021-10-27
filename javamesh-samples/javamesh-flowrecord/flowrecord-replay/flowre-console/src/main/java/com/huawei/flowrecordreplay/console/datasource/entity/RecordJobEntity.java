/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 录制任务对象
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@Getter
@Setter
public class RecordJobEntity {
    /**
     * 任务ID
     */
    private String jobId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 应用名称
     */
    private String application;

    /**
     * 机器ip列表
     */
    private List<String> machineList;

    /**
     * 方法列表
     */
    private List<String> methodList;

    /**
     * 任务触发或终止标识
     */
    private boolean trigger;

    /**
     * 保留字段，任务额外信息
     */
    private String extra;

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
     * 任务下发的时间戳
     */
    private Long timeStamp;

    /**
     * 任务状态
     */
    private String status;
}
