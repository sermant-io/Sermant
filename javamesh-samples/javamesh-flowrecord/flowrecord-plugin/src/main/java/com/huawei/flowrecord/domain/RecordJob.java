/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * recordjob内容，通用于flowreord-console端
 *
 */
@Getter
@Setter
public class RecordJob implements Serializable {
    public static RecordJob recordJob = null;

    private String jobId;

    private String application;

    private List<String> machineList;

    private List<String> methodList;

    private boolean trigger = false;

    private String extra;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private Long timeStamp;

    private String status;
}
