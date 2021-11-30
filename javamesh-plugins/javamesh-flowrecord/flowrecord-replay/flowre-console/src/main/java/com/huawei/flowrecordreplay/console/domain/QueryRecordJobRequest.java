/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 录制任务请求
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@Getter
@Setter
public class QueryRecordJobRequest {
    /**
     * 任务名称
     */
    private String name;

    /**
     * 应用名称
     */
    private String application;

    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date from;

    /**
     * 任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date to;
}
